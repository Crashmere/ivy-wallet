package com.ivy.reports

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.Account
import com.ivy.data.model.ExternalFile
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.TransactionType
import com.ivy.ui.resource.ResourceProvider
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Tag
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.getTransactionType
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.csv.ExportCsvUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.ExchangeTransactionAmountUseCase
import com.ivy.domain.usecase.tag.GetTagsUseCase
import com.ivy.domain.usecase.tag.SearchTagsUseCase
import com.ivy.domain.usecase.planned.PayOrSkipPlannedTransactionByIdUseCase
import com.ivy.domain.usecase.planned.PayOrSkipPlannedTransactionsByIdsUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsByTagsUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsUseCase
import com.ivy.domain.usecase.transaction.MapTransactionsToLegacyTransactionsUseCase
import com.ivy.ui.period.PeriodState
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.R
import com.ivy.ui.platform.FilePicker
import com.ivy.ui.preferences.asEnabledState
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.transaction.BuildTransactionHistoryItemsUseCase
import com.ivy.domain.usecase.transaction.CalculateTransactionsIncomeExpenseUseCase
import com.ivy.data.model.IncomeExpenseTransferPair
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

private val exportTimestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")
private val UnspecifiedCategoryColorArgb = 0xFF939199.toInt()

@Stable
@HiltViewModel
internal class ReportViewModel @Inject internal constructor(
    private val payOrSkipPlannedTransactionByIdUseCase: PayOrSkipPlannedTransactionByIdUseCase,
    private val payOrSkipPlannedTransactionsByIdsUseCase: PayOrSkipPlannedTransactionsByIdsUseCase,
    private val periodState: PeriodState,
    private val exchangeTransactionAmountUseCase: ExchangeTransactionAmountUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val buildTransactionHistoryItemsUseCase: BuildTransactionHistoryItemsUseCase,
    private val calculateTransactionsIncomeExpenseUseCase: CalculateTransactionsIncomeExpenseUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTransactionsByTagsUseCase: GetTransactionsByTagsUseCase,
    private val mapTransactionsToLegacyTransactionsUseCase: MapTransactionsToLegacyTransactionsUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val searchTagsUseCase: SearchTagsUseCase,
    private val exportCsvUseCase: ExportCsvUseCase,
    private val resourceProvider: ResourceProvider,
    private val preferenceToggles: PreferenceToggleCatalog,
    private val preferenceToggleService: PreferenceToggleService,
    private val filePicker: FilePicker
) : ComposeViewModel<ReportScreenState, ReportScreenEvent>() {
    private val unspecifiedCategory =
        Category(
            name = NotBlankTrimmedString.unsafe(resourceProvider.getString(R.string.unspecified)),
            color = ColorInt(UnspecifiedCategoryColorArgb),
            icon = null,
            id = CategoryId(UUID.randomUUID()),
            orderNum = 0.0,
        )
    private var baseCurrency by mutableStateOf("")
    private var categories by mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private var historyIncomeExpense by mutableStateOf(IncomeExpenseTransferPair.zero())
    private var filter by mutableStateOf<ReportFilter?>(null)
    private var balance by mutableDoubleStateOf(0.0)
    private var income by mutableDoubleStateOf(0.0)
    private var expenses by mutableDoubleStateOf(0.0)
    private var history by mutableStateOf<ImmutableList<TransactionHistoryItem>>(persistentListOf())
    private var upcoming by mutableStateOf(
        ReportDueSection(
            transactions = persistentListOf(),
            expanded = false,
            income = 0.0,
            expenses = 0.0,
        )
    )
    private var overdue by mutableStateOf(
        ReportDueSection(
            transactions = persistentListOf(),
            expanded = false,
            income = 0.0,
            expenses = 0.0,
        )
    )
    private var accountModels: ImmutableList<Account> = persistentListOf()
    private var accounts by mutableStateOf<ImmutableList<ReportAccount>>(persistentListOf())
    private var loading by mutableStateOf(false)
    private var accountIdFilters by mutableStateOf<ImmutableList<UUID>>(persistentListOf())
    private var transactionSummary by mutableStateOf(ReportTransactionSummary())
    private var filterOverlayVisible by mutableStateOf(false)
    private var showTransfersAsIncExpCheckbox by mutableStateOf(false)
    private var treatTransfersAsIncExp by mutableStateOf(false)
    private var allTags by mutableStateOf<ImmutableList<Tag>>(persistentListOf())

    private var tagSearchJob: Job? = null
    private val tagSearchDebounceTimeInMillis: Long = 500
    private val _uiEvents = MutableSharedFlow<ReportUiEvent>()
    val uiEvents: SharedFlow<ReportUiEvent> = _uiEvents.asSharedFlow()

    private data class ReportValues(
        val income: Double,
        val expense: Double,
        val upcomingIncomeExpense: IncomeExpenseTransferPair,
        val overdueIncomeExpense: IncomeExpenseTransferPair,
        val history: ImmutableList<TransactionHistoryItem>,
        val upcomingTransactions: ImmutableList<LegacyTransaction>,
        val overdueTransactions: ImmutableList<LegacyTransaction>,
        val accounts: ImmutableList<Account>,
        val reportFilter: ReportFilter?,
        val accountIdFilters: ImmutableList<UUID>,
        val transactionSummary: ReportTransactionSummary,
        val balance: Double,
    )

    @Composable
    fun getShouldShowAccountSpecificColorInTransactions(): Boolean {
        val preference = preferenceToggles.showAccountColorsInTransactions
        return preferenceToggleService.enabledFlow(preference)
            .asEnabledState(preference.defaultValue)
    }

    @Composable
    override fun uiState(): ReportScreenState {
        LaunchedEffect(Unit) {
            start()
        }

        return ReportScreenState(
            categories = categories,
            accounts = accounts,
            accountIdFilters = accountIdFilters,
            balance = balance,
            baseCurrency = baseCurrency,
            expenses = expenses,
            filter = filter,
            filterOverlayVisible = filterOverlayVisible,
            history = history,
            income = income,
            loading = loading,
            overdue = overdue,
            showTransfersAsIncExpCheckbox = showTransfersAsIncExpCheckbox,
            transactionSummary = transactionSummary,
            treatTransfersAsIncExp = treatTransfersAsIncExp,
            upcoming = upcoming,
            allTags = allTags,
            showAccountColorsInTransactions = getShouldShowAccountSpecificColorInTransactions()
        )
    }

    override fun onEvent(event: ReportScreenEvent) {
        viewModelScope.launch(Dispatchers.Default) {
            when (event) {
                is ReportScreenEvent.OnFilter -> setFilter(event.filter)
                ReportScreenEvent.OnExport -> export()
                is ReportScreenEvent.OnPayOrGetTransaction -> payOrGetTransaction(event.transactionId)
                is ReportScreenEvent.SkipTransaction -> skipTransaction(event.transactionId)
                is ReportScreenEvent.SkipTransactions -> skipTransactions(event.transactionIds)
                is ReportScreenEvent.OnOverdueExpanded -> setOverdueExpandedValue(event.overdueExpanded)
                is ReportScreenEvent.OnUpcomingExpanded -> setUpcomingExpandedValue(event.upcomingExpanded)
                is ReportScreenEvent.OnFilterOverlayVisible -> setFilterOverlayVisibleValue(event.filterOverlayVisible)
                is ReportScreenEvent.OnTreatTransfersAsIncomeExpense -> onTreatTransfersAsIncomeExpense(
                    event.transfersAsIncomeExpense
                )

                is ReportScreenEvent.OnTagSearch -> onTagSearch(event.data)
            }
        }
    }

    private suspend fun onTagSearch(query: String) {
        withContext(Dispatchers.IO) {
            tagSearchJob?.cancelAndJoin()
            delay(tagSearchDebounceTimeInMillis)
            tagSearchJob = launch(Dispatchers.IO) {
                NotBlankTrimmedString.from(query.lowercase(Locale.getDefault()))
                    .fold(
                        ifRight = {
                            allTags =
                                searchTagsUseCase(it).toImmutableList()
                        },
                        ifLeft = {
                            allTags = getTagsUseCase().toImmutableList()
                        }
                    )
            }
        }
    }

    private fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            baseCurrency = getBaseCurrencyCode()
            accountModels = getAccountsUseCase().toImmutableList()
            accounts = accountModels.map { it.toReportAccount() }.toImmutableList()
            categories =
                (listOf(unspecifiedCategory) + getCategoriesUseCase()).toImmutableList()
            allTags = getTagsUseCase().toImmutableList()
        }
    }

    private suspend fun setFilter(reportFilter: ReportFilter?) {
        withContext(Dispatchers.IO) {
            val scope = this
            if (reportFilter == null) {
                setReportValues(
                    ReportValues(
                        income = 0.00,
                        expense = 0.00,
                        upcomingIncomeExpense = IncomeExpenseTransferPair.zero(),
                        overdueIncomeExpense = IncomeExpenseTransferPair.zero(),
                        history = persistentListOf(),
                        upcomingTransactions = persistentListOf(),
                        overdueTransactions = persistentListOf(),
                        accounts = getAccountsUseCase().toImmutableList(),
                        reportFilter = filter,
                        accountIdFilters = persistentListOf(),
                        transactionSummary = ReportTransactionSummary(),
                        balance = 0.00
                    )
                )
                return@withContext
            }

            if (!reportFilter.validate()) return@withContext
            val allAccounts = getAccountsUseCase().toImmutableList()
            val selectedAccounts = allAccounts.filter { it.id.value in reportFilter.accountIds }
            if (selectedAccounts.isEmpty()) return@withContext
            val legacyAccounts = allAccounts.toLegacyAccounts()
            val baseCurrency = baseCurrency
            loading = true

            val transactionsList = filterTransactions(
                baseCurrency = baseCurrency,
                accounts = legacyAccounts,
                filter = reportFilter
            )

            val historyTransactions = transactionsList
                .sortedByDescending { it.time }

            val historyWithDateDividers = scope.async {
                buildTransactionHistoryItemsUseCase(
                    baseCurrency = baseCurrency,
                    transactions = historyTransactions
                )
            }

            historyIncomeExpense = calculateTransactionsIncomeExpenseUseCase(
                transactions = historyTransactions,
                accounts = legacyAccounts,
                baseCurrency = baseCurrency
            )

            val displayIncome = historyIncomeExpense.income.toDouble() +
                    if (treatTransfersAsIncExp) historyIncomeExpense.transferIncome.toDouble() else 0.0

            val displayExpenses = historyIncomeExpense.expense.toDouble() +
                    if (treatTransfersAsIncExp) historyIncomeExpense.transferExpense.toDouble() else 0.0

            val displayBalance = calculateBalance(historyIncomeExpense).toDouble()

            val accountFilterIdList = scope.async { selectedAccounts.map { it.id.value } }

            val timeNowUTC = utcNow()

            // Upcoming
            val upcomingTransactionsList = transactionsList
                .filter {
                    !it.settled && it.time.atZone(ZoneId.systemDefault()).toLocalDateTime()
                        .isAfter(timeNowUTC)
                }
                .sortedBy { it.time }
                .toImmutableList()

            val upcomingIncomeExpense = calculateTransactionsIncomeExpenseUseCase(
                transactions = upcomingTransactionsList,
                accounts = legacyAccounts,
                baseCurrency = baseCurrency
            )
            // Overdue
            val overdue = transactionsList.filter {
                !it.settled && it.time.atZone(ZoneId.systemDefault()).toLocalDateTime()
                    .isBefore(timeNowUTC)
            }.sortedByDescending {
                it.time
            }.toImmutableList()
            val overdueIncomeExpense = calculateTransactionsIncomeExpenseUseCase(
                transactions = overdue,
                accounts = legacyAccounts,
                baseCurrency = baseCurrency
            )

            setReportValues(
                ReportValues(
                    income = displayIncome,
                    expense = displayExpenses,
                    upcomingIncomeExpense = upcomingIncomeExpense,
                    overdueIncomeExpense = overdueIncomeExpense,
                    history = historyWithDateDividers.await().toImmutableList(),
                    upcomingTransactions = mapTransactionsToLegacyTransactionsUseCase(upcomingTransactionsList)
                        .toImmutableList(),
                    overdueTransactions = mapTransactionsToLegacyTransactionsUseCase(overdue).toImmutableList(),
                    accounts = allAccounts,
                    reportFilter = reportFilter,
                    accountIdFilters = accountFilterIdList.await().toImmutableList(),
                    transactionSummary = transactionsList.toReportTransactionSummary(),
                    balance = displayBalance
                )
            )

            loading = false
        }
    }

    private fun setReportValues(values: ReportValues) {
        this.income = values.income
        this.expenses = values.expense
        this.history = values.history
        this.upcoming = upcoming.copy(
            transactions = values.upcomingTransactions,
            income = values.upcomingIncomeExpense.income.toDouble(),
            expenses = values.upcomingIncomeExpense.expense.toDouble(),
        )
        this.overdue = overdue.copy(
            transactions = values.overdueTransactions,
            income = values.overdueIncomeExpense.income.toDouble(),
            expenses = values.overdueIncomeExpense.expense.toDouble(),
        )
        this.accountModels = values.accounts
        this.accounts = values.accounts.map { it.toReportAccount() }.toImmutableList()
        this.filter = values.reportFilter
        this.accountIdFilters = values.accountIdFilters
        this.transactionSummary = values.transactionSummary
        this.balance = values.balance
        this.showTransfersAsIncExpCheckbox =
            values.reportFilter?.transactionTypes?.contains(TransactionType.TRANSFER) ?: false
    }

    private suspend fun filterTransactions(
        baseCurrency: String,
        accounts: List<LegacyAccount>,
        filter: ReportFilter,
    ): ImmutableList<Transaction> {
        val filterAccountIds = filter.accountIds.toSet()
        val filterCategoryIds = filter.categoryIds
            .map { it.takeUnless { categoryId -> categoryId == unspecifiedCategory.id } }
            .toSet()
        val filterRange =
            filter.period?.let(periodState::rangeOf)

        val transactions = if (filter.includedTags.isNotEmpty()) {
            getTransactionsByTagsUseCase(filter.includedTags)
        } else {
            getTransactionsUseCase()
        }

        val excludeableByTagTransactionsIds = if (filter.excludedTags.isNotEmpty()) {
            getTransactionsByTagsUseCase(filter.excludedTags).map { it.id }
        } else {
            emptyList()
        }

        return transactions
            .filter { !excludeableByTagTransactionsIds.contains(it.id) }
            .filter {
                filter.transactionTypes.contains(it.getTransactionType())
            }
            .filter {
                // Filter by Time Period

                filterRange ?: return@filter false

                filterRange.includes(it.time)
            }
            .filter { transaction ->
                // Filter by Accounts
                when (transaction) {
                    is Transfer -> {
                        filterAccountIds.contains(transaction.fromAccount.value) || // Transfers Out
                                (filterAccountIds.contains(transaction.toAccount.value)) // Transfers In
                    }

                    is Expense -> {
                        filterAccountIds.contains(transaction.account.value)
                    }

                    is Income -> {
                        filterAccountIds.contains(transaction.account.value)
                    }
                }
            }
            .filter { transaction ->
                // Filter by Categories

                filterCategoryIds.contains(transaction.category) ||
                        transaction.getTransactionType() == TransactionType.TRANSFER
            }
            .filterByAmount(baseCurrency, accounts, filter)
            .filter {
                // Filter by Included Keywords

                val includeKeywords = filter.includeKeywords
                if (includeKeywords.isEmpty()) return@filter true

                it.title?.let { title ->
                    includeKeywords.forEach { keyword ->
                        if (title.value.containsLowercase(keyword)) {
                            return@filter true
                        }
                    }
                }

                it.description?.let { description ->
                    includeKeywords.forEach { keyword ->
                        if (description.value.containsLowercase(keyword)) {
                            return@filter true
                        }
                    }
                }

                false
            }
            .filter {
                // Filter by Excluded Keywords

                val excludedKeywords = filter.excludeKeywords
                if (excludedKeywords.isEmpty()) return@filter true

                it.title?.let { title ->
                    excludedKeywords.forEach { keyword ->
                        if (title.value.containsLowercase(keyword)) {
                            return@filter false
                        }
                    }
                }
                it.description?.let { description ->
                    excludedKeywords.forEach { keyword ->
                        if (description.value.containsLowercase(keyword)) {
                            return@filter false
                        }
                    }
                }
                true
            }.toImmutableList()
    }

    private suspend fun List<Transaction>.filterByAmount(
        baseCurrency: String,
        accounts: List<LegacyAccount>,
        filter: ReportFilter
    ): List<Transaction> {
        val amountFilteredTransactions = mutableListOf<Transaction>()
        for (transaction in this) {
            val transactionAmountBaseCurrency = exchangeTransactionAmountUseCase(
                transaction = transaction,
                accounts = accounts,
                baseCurrency = baseCurrency,
            ).toDouble()

            if ((filter.minAmount == null || transactionAmountBaseCurrency >= filter.minAmount) &&
                (filter.maxAmount == null || transactionAmountBaseCurrency <= filter.maxAmount)
            ) {
                amountFilteredTransactions += transaction
            }
        }
        return amountFilteredTransactions
    }

    private fun List<Transaction>.toReportTransactionSummary(): ReportTransactionSummary {
        return ReportTransactionSummary(
            transactionIds = map { it.id.value }.toImmutableList(),
            incomeTransactionCount = count { it.getTransactionType() == TransactionType.INCOME },
            expenseTransactionCount = count { it.getTransactionType() == TransactionType.EXPENSE },
        )
    }

    private fun String.containsLowercase(anotherString: String): Boolean {
        return this.lowercase(Locale.getDefault()).contains(anotherString.lowercase(Locale.getDefault()))
    }

    private fun calculateBalance(incomeExpenseTransferPair: IncomeExpenseTransferPair): BigDecimal {
        return incomeExpenseTransferPair.income + incomeExpenseTransferPair.transferIncome - incomeExpenseTransferPair.expense - incomeExpenseTransferPair.transferExpense
    }

    private suspend fun export() {
        val filter = filter ?: return
        if (!filter.validate()) return

        filePicker.createFile(
            "IvyWalletReport-${utcTimestamp()}.csv"
        ) { fileUri ->
            viewModelScope.launch {
                loading = true

                exportCsvUseCase.exportToFile(
                    outputFile = ExternalFile(fileUri.toString()),
                    exportScope = {
                        filterTransactions(
                            baseCurrency = baseCurrency,
                            accounts = accountModels.toLegacyAccounts(),
                            filter = filter
                        )
                    }
                )

                _uiEvents.emit(ReportUiEvent.ShareCsvFile(fileUri))

                loading = false
            }
        }
    }

    private fun setUpcomingExpandedValue(expanded: Boolean) {
        upcoming = upcoming.copy(expanded = expanded)
    }

    private fun utcNow() =
        Instant.now()
            .atZone(ZoneOffset.UTC)
            .toLocalDateTime()

    private fun utcTimestamp(): String = utcNow().format(exportTimestampFormatter)

    private fun setOverdueExpandedValue(expanded: Boolean) {
        overdue = overdue.copy(expanded = expanded)
    }

    private suspend fun payOrGetTransaction(transactionId: UUID) {
        withContext(Dispatchers.Main) {
            if (payOrSkipPlannedTransactionByIdUseCase(transactionId)) {
                start()
                setFilter(filter)
            }
        }
    }

    private fun setFilterOverlayVisibleValue(visible: Boolean) {
        filterOverlayVisible = visible
    }

    private fun onTreatTransfersAsIncomeExpense(transfersAsIncExp: Boolean) {
        income = historyIncomeExpense.income.toDouble() +
                if (transfersAsIncExp) historyIncomeExpense.transferIncome.toDouble() else 0.0
        expenses = historyIncomeExpense.expense.toDouble() +
                if (transfersAsIncExp) historyIncomeExpense.transferExpense.toDouble() else 0.0
        treatTransfersAsIncExp = transfersAsIncExp
    }

    private suspend fun skipTransaction(transactionId: UUID) {
        withContext(Dispatchers.Main) {
            val paidTransaction = payOrSkipPlannedTransactionByIdUseCase(
                transactionId = transactionId,
                skipTransaction = true
            )
            if (paidTransaction) {
                start()
                setFilter(filter)
            }
        }
    }

    private suspend fun skipTransactions(transactionIds: List<UUID>) {
        withContext(Dispatchers.Main) {
            val paidTransactions = payOrSkipPlannedTransactionsByIdsUseCase(
                transactionIds = transactionIds,
                skipTransaction = true
            )
            if (paidTransactions > 0) {
                start()
                setFilter(filter)
            }
        }
    }
}

private fun Iterable<Account>.toLegacyAccounts(): ImmutableList<LegacyAccount> {
    return map { it.toLegacyAccount() }.toImmutableList()
}

private fun Account.toLegacyAccount() = LegacyAccount(
    name = name.value,
    currency = asset.code,
    color = color.value,
    icon = icon?.id,
    orderNum = orderNum,
    includeInBalance = includeInBalance,
    isDeleted = false,
    id = id.value,
)

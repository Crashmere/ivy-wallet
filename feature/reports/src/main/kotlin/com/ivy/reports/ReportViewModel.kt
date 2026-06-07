package com.ivy.reports

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewModelScope
import com.ivy.data.api.file.ExternalFile
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.legacy.TransactionHistoryItem
import com.ivy.data.model.TransactionType
import com.ivy.ui.resource.ResourceProvider
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Tag
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.preferences.toggles.PreferenceToggleRepository
import com.ivy.domain.preferences.toggles.PreferenceToggles
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.csv.ExportCsvUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.usecase.tag.GetTagsUseCase
import com.ivy.domain.usecase.tag.SearchTagsUseCase
import com.ivy.domain.usecase.planned.PayOrSkipLegacyPlannedTransactionUseCase
import com.ivy.domain.usecase.planned.PayOrSkipLegacyPlannedTransactionsUseCase
import com.ivy.domain.usecase.planned.PayOrSkipPlannedTransactionUseCase
import com.ivy.domain.usecase.planned.PayOrSkipPlannedTransactionsUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsByTagsUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsUseCase
import com.ivy.domain.usecase.transaction.MapTransactionsToLegacyUseCase
import com.ivy.legacy.ui.state.PeriodState
import com.ivy.data.model.legacy.Account
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.R
import com.ivy.ui.platform.FilePicker
import com.ivy.ui.platform.FileSharer
import com.ivy.ui.preferences.asEnabledState
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.domain.usecase.transaction.BuildTransactionHistoryItemsUseCase
import com.ivy.domain.usecase.transaction.CalculateTransactionsIncomeExpenseUseCase
import com.ivy.data.model.legacy.IncomeExpenseTransferPair
import com.ivy.domain.exchange.ExchangeData
import com.ivy.domain.transaction.getTransactionType
import com.ivy.domain.transaction.getValue
import com.ivy.domain.transaction.transactionCurrency
import com.ivy.domain.util.orZero
import com.ivy.legacy.ui.theme.Gray
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
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

@Stable
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val payOrSkipPlannedTransactionUseCase: PayOrSkipPlannedTransactionUseCase,
    private val payOrSkipPlannedTransactionsUseCase: PayOrSkipPlannedTransactionsUseCase,
    private val payOrSkipLegacyPlannedTransactionUseCase: PayOrSkipLegacyPlannedTransactionUseCase,
    private val payOrSkipLegacyPlannedTransactionsUseCase: PayOrSkipLegacyPlannedTransactionsUseCase,
    private val periodState: PeriodState,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val buildTransactionHistoryItemsUseCase: BuildTransactionHistoryItemsUseCase,
    private val calculateTransactionsIncomeExpenseUseCase: CalculateTransactionsIncomeExpenseUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTransactionsByTagsUseCase: GetTransactionsByTagsUseCase,
    private val mapTransactionsToLegacyUseCase: MapTransactionsToLegacyUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val searchTagsUseCase: SearchTagsUseCase,
    private val exportCsvUseCase: ExportCsvUseCase,
    private val resourceProvider: ResourceProvider,
    private val preferenceToggles: PreferenceToggles,
    private val preferenceToggleRepository: PreferenceToggleRepository,
    private val filePicker: FilePicker
) : ComposeViewModel<ReportScreenState, ReportScreenEvent>() {
    private val unSpecifiedCategory =
        Category(
            name = NotBlankTrimmedString.unsafe(resourceProvider.getString(R.string.unspecified)),
            color = ColorInt(Gray.toArgb()),
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
    private var upcomingIncome by mutableDoubleStateOf(0.0)
    private var upcomingExpenses by mutableDoubleStateOf(0.0)
    private var overdueIncome by mutableDoubleStateOf(0.0)
    private var overdueExpenses by mutableDoubleStateOf(0.0)
    private var history by mutableStateOf<ImmutableList<TransactionHistoryItem>>(persistentListOf())
    private var upcomingTransactions by
    mutableStateOf<ImmutableList<LegacyTransaction>>(persistentListOf())
    private var overdueTransactions by
    mutableStateOf<ImmutableList<LegacyTransaction>>(persistentListOf())
    private var accounts by mutableStateOf<ImmutableList<Account>>(persistentListOf())
    private var upcomingExpanded by mutableStateOf(false)
    private var overdueExpanded by mutableStateOf(false)
    private var loading by mutableStateOf(false)
    private var accountIdFilters by mutableStateOf<ImmutableList<UUID>>(persistentListOf())
    private var transactions by mutableStateOf<ImmutableList<LegacyTransaction>>(persistentListOf())
    private var filterOverlayVisible by mutableStateOf(false)
    private var showTransfersAsIncExpCheckbox by mutableStateOf(false)
    private var treatTransfersAsIncExp by mutableStateOf(false)
    private var allTags by mutableStateOf<ImmutableList<Tag>>(persistentListOf())

    private var tagSearchJob: Job? = null
    private val tagSearchDebounceTimeInMills: Long = 500

    @Composable
    fun getShouldShowAccountSpecificColorInTransactions(): Boolean {
        val preference = preferenceToggles.showAccountColorsInTransactions
        return preferenceToggleRepository.enabledFlow(preference)
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
            overdueExpanded = overdueExpanded,
            overdueExpenses = overdueExpenses,
            overdueIncome = overdueIncome,
            overdueTransactions = overdueTransactions,
            showTransfersAsIncExpCheckbox = showTransfersAsIncExpCheckbox,
            transactions = transactions,
            treatTransfersAsIncExp = treatTransfersAsIncExp,
            upcomingExpanded = upcomingExpanded,
            upcomingExpenses = upcomingExpenses,
            upcomingIncome = upcomingIncome,
            upcomingTransactions = upcomingTransactions,
            allTags = allTags,
            showAccountColorsInTransactions = getShouldShowAccountSpecificColorInTransactions()
        )
    }

    override fun onEvent(event: ReportScreenEvent) {
        viewModelScope.launch(Dispatchers.Default) {
            when (event) {
                is ReportScreenEvent.OnFilter -> setFilter(event.filter)
                is ReportScreenEvent.OnExport -> export(event.fileSharer)
                is ReportScreenEvent.OnPayOrGet -> payOrGet(event.transaction)
                is ReportScreenEvent.SkipTransaction -> skipTransaction(event.transaction)
                is ReportScreenEvent.SkipTransactions -> skipTransactions(event.transactions)
                is ReportScreenEvent.OnPayOrGetLegacy -> payOrGetLegacy(event.transaction)
                is ReportScreenEvent.SkipTransactionLegacy -> skipTransactionLegacy(event.transaction)
                is ReportScreenEvent.SkipTransactionsLegacy -> skipTransactionsLegacy(event.transactions)
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
            delay(tagSearchDebounceTimeInMills) // Debounce effect
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
            accounts = getLegacyAccountsUseCase()
            categories =
                (listOf(unSpecifiedCategory) + getCategoriesUseCase()).toImmutableList()
            allTags = getTagsUseCase().toImmutableList()
        }
    }

    private suspend fun setFilter(reportFilter: ReportFilter?) {
        withContext(Dispatchers.IO) {
            val scope = this
            if (reportFilter == null) {
                setReportValues(
                    income = 0.00,
                    expense = 0.00,
                    upcomingIncomeExpenseTransferPair = IncomeExpenseTransferPair.zero(),
                    overDueIncomeExpenseTransferPair = IncomeExpenseTransferPair.zero(),
                    history = persistentListOf(),
                    upcomingTransactions = persistentListOf(),
                    overdueTransactions = persistentListOf(),
                    accounts = getLegacyAccountsUseCase(),
                    reportFilter = filter,
                    accountIdFilters = persistentListOf(),
                    transactions = persistentListOf(),
                    balanceValue = 0.00
                )
                return@withContext
            }

            if (!reportFilter.validate()) return@withContext
            val selectedAccounts = reportFilter.accounts
            val baseCurrency = baseCurrency
            loading = true

            val transactionsList = filterTransactions(
                baseCurrency = baseCurrency,
                accounts = selectedAccounts,
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
                accounts = selectedAccounts,
                baseCurrency = baseCurrency
            )

            val displayIncome = historyIncomeExpense.income.toDouble() +
                    if (treatTransfersAsIncExp) historyIncomeExpense.transferIncome.toDouble() else 0.0

            val displayExpenses = historyIncomeExpense.expense.toDouble() +
                    if (treatTransfersAsIncExp) historyIncomeExpense.transferExpense.toDouble() else 0.0

            val displayBalance = calculateBalance(historyIncomeExpense).toDouble()

            val accountFilterIdList = scope.async { selectedAccounts.map { it.id } }

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
                accounts = selectedAccounts,
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
                accounts = selectedAccounts,
                baseCurrency = baseCurrency
            )

            setReportValues(
                income = displayIncome,
                expense = displayExpenses,
                upcomingIncomeExpenseTransferPair = upcomingIncomeExpense,
                overDueIncomeExpenseTransferPair = overdueIncomeExpense,
                history = historyWithDateDividers.await().toImmutableList(),
                upcomingTransactions = mapTransactionsToLegacyUseCase(upcomingTransactionsList)
                    .toImmutableList(),
                overdueTransactions = mapTransactionsToLegacyUseCase(overdue).toImmutableList(),
                accounts = selectedAccounts.toImmutableList(),
                reportFilter = reportFilter,
                accountIdFilters = accountFilterIdList.await().toImmutableList(),
                transactions = mapTransactionsToLegacyUseCase(transactionsList).toImmutableList(),
                balanceValue = displayBalance
            )

            loading = false
        }
    }

    private fun setReportValues(
        income: Double,
        expense: Double,
        upcomingIncomeExpenseTransferPair: IncomeExpenseTransferPair,
        overDueIncomeExpenseTransferPair: IncomeExpenseTransferPair,
        history: ImmutableList<TransactionHistoryItem>,
        upcomingTransactions: ImmutableList<LegacyTransaction>,
        overdueTransactions: ImmutableList<LegacyTransaction>,
        accounts: ImmutableList<Account>,
        reportFilter: ReportFilter? = null,
        accountIdFilters: ImmutableList<UUID>,
        transactions: ImmutableList<LegacyTransaction>,
        balanceValue: Double
    ) {
        this.income = income
        this.expenses = expense
        this.upcomingExpenses = upcomingIncomeExpenseTransferPair.expense.toDouble()
        this.upcomingIncome = upcomingIncomeExpenseTransferPair.income.toDouble()
        this.overdueIncome = overDueIncomeExpenseTransferPair.income.toDouble()
        this.overdueExpenses = overDueIncomeExpenseTransferPair.expense.toDouble()
        this.history = history
        this.upcomingTransactions = upcomingTransactions
        this.overdueTransactions = overdueTransactions
        this.accounts = accounts
        this.filter = reportFilter
        this.accountIdFilters = accountIdFilters
        this.transactions = transactions
        this.balance = balanceValue
        this.showTransfersAsIncExpCheckbox =
            reportFilter?.trnTypes?.contains(TransactionType.TRANSFER) ?: false
    }

    private suspend fun filterTransactions(
        baseCurrency: String,
        accounts: List<Account>,
        filter: ReportFilter,
    ): ImmutableList<Transaction> {
        val filterAccountIds = filter.accounts.map { it.id }
        val filterCategoryIds =
            filter.categories.map { if (it.id.value == unSpecifiedCategory.id.value) null else it.id }
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
                filter.trnTypes.contains(it.getTransactionType())
            }
            .filter {
                // Filter by Time Period

                filterRange ?: return@filter false

                filterRange.includes(it.time)
            }
            .filter { trn ->
                // Filter by Accounts
                when (trn) {
                    is Transfer -> {
                        filterAccountIds.contains(trn.fromAccount.value) || // Transfers Out
                                (filterAccountIds.contains(trn.toAccount.value)) // Transfers In
                    }

                    is Expense -> {
                        filterAccountIds.contains(trn.account.value)
                    }

                    is Income -> {
                        filterAccountIds.contains(trn.account.value)
                    }
                }
            }
            .filter { trn ->
                // Filter by Categories

                filterCategoryIds.contains(trn.category) ||
                        trn.getTransactionType() == TransactionType.TRANSFER
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
        accounts: List<Account>,
        filter: ReportFilter
    ): List<Transaction> {
        val amountFilteredTransactions = mutableListOf<Transaction>()
        for (transaction in this) {
            val trnAmountBaseCurrency = exchangeAmountUseCase(
                data = ExchangeData(
                    baseCurrency = baseCurrency,
                    fromCurrency = transactionCurrency(transaction, accounts, baseCurrency),
                ),
                amount = transaction.getValue()
            ).orZero().toDouble()

            if ((filter.minAmount == null || trnAmountBaseCurrency >= filter.minAmount) &&
                (filter.maxAmount == null || trnAmountBaseCurrency <= filter.maxAmount)
            ) {
                amountFilteredTransactions += transaction
            }
        }
        return amountFilteredTransactions
    }

    private fun String.containsLowercase(anotherString: String): Boolean {
        return this.lowercase(Locale.getDefault()).contains(anotherString.lowercase(Locale.getDefault()))
    }

    private fun calculateBalance(incomeExpenseTransferPair: IncomeExpenseTransferPair): BigDecimal {
        return incomeExpenseTransferPair.income + incomeExpenseTransferPair.transferIncome - incomeExpenseTransferPair.expense - incomeExpenseTransferPair.transferExpense
    }

    private suspend fun export(fileSharer: FileSharer) {
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
                            accounts = accounts,
                            filter = filter
                        )
                    }
                )

                fileSharer.shareCSVFile(
                    fileUri = fileUri
                )

                loading = false
            }
        }
    }

    private fun setUpcomingExpandedValue(expanded: Boolean) {
        upcomingExpanded = expanded
    }

    private fun utcNow() =
        Instant.now()
            .atZone(ZoneOffset.UTC)
            .toLocalDateTime()

    private fun utcTimestamp(): String = utcNow().format(exportTimestampFormatter)

    private fun setOverdueExpandedValue(expanded: Boolean) {
        overdueExpanded = expanded
    }

    private suspend fun payOrGet(transaction: Transaction) {
        withContext(Dispatchers.Main) {
            if (payOrSkipPlannedTransactionUseCase(transaction) != null) {
                start()
                setFilter(filter)
            }
        }
    }

    private suspend fun payOrGetLegacy(transaction: com.ivy.data.model.legacy.Transaction) {
        withContext(Dispatchers.Main) {
            if (payOrSkipLegacyPlannedTransactionUseCase(transaction) != null) {
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

    private suspend fun skipTransaction(transaction: Transaction) {
        withContext(Dispatchers.Main) {
            val paidTransaction = payOrSkipPlannedTransactionUseCase(
                transaction = transaction,
                skipTransaction = true
            )
            if (paidTransaction != null) {
                start()
                setFilter(filter)
            }
        }
    }

    private suspend fun skipTransactionLegacy(transaction: com.ivy.data.model.legacy.Transaction) {
        withContext(Dispatchers.Main) {
            val paidTransaction = payOrSkipLegacyPlannedTransactionUseCase(
                transaction = transaction,
                skipTransaction = true
            )
            if (paidTransaction != null) {
                start()
                setFilter(filter)
            }
        }
    }

    private suspend fun skipTransactions(transactions: List<Transaction>) {
        withContext(Dispatchers.Main) {
            val paidTransactions = payOrSkipPlannedTransactionsUseCase(
                transactions = transactions,
                skipTransaction = true
            )
            if (paidTransactions.isNotEmpty()) {
                start()
                setFilter(filter)
            }
        }
    }

    private suspend fun skipTransactionsLegacy(transactions: List<com.ivy.data.model.legacy.Transaction>) {
        withContext(Dispatchers.Main) {
            val paidTransactions = payOrSkipLegacyPlannedTransactionsUseCase(
                transactions = transactions,
                skipTransaction = true
            )
            if (paidTransactions.isNotEmpty()) {
                start()
                setFilter(filter)
            }
        }
    }
}

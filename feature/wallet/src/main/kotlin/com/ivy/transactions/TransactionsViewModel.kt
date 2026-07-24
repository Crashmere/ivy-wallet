package com.ivy.transactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.TransactionHistoryTransaction
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Transaction
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getToAccount
import com.ivy.data.model.getTransactionType
import com.ivy.ui.resource.ResourceProvider
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Tag
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.usecase.account.DeleteAccountUseCase
import com.ivy.domain.usecase.category.DeleteCategoryUseCase
import com.ivy.domain.usecase.category.CategoryTransactionsSummary
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.category.GetCategoryUseCase
import com.ivy.domain.usecase.category.GetCategoryTransactionsSummaryUseCase
import com.ivy.domain.usecase.category.GetUnspecifiedCategoryTransactionsSummaryUseCase
import com.ivy.domain.usecase.category.UpdateCategoryUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.ui.period.PeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.data.model.toCloseTimeRange
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.modal.AccountModalSaveData
import com.ivy.ui.R
import com.ivy.ui.preferences.asEnabledState
import com.ivy.domain.usecase.account.CalculateAccountBalanceUseCase
import com.ivy.domain.usecase.account.CalculateAccountIncomeExpenseUseCase
import com.ivy.domain.usecase.account.GetAccountTransactionsUseCase
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.account.GetAccountOverdueTransactionsSummaryUseCase
import com.ivy.domain.usecase.account.GetAccountUpcomingTransactionsSummaryUseCase
import com.ivy.domain.usecase.account.UpdateAccountWithBalanceUseCase
import com.ivy.domain.usecase.planned.PayOrSkipPlannedTransactionByIdUseCase
import com.ivy.domain.usecase.planned.PayOrSkipPlannedTransactionsByIdsUseCase
import com.ivy.domain.usecase.settings.GetTransfersAsIncomeExpensePreferenceUseCase
import com.ivy.domain.usecase.transaction.BuildTransactionHistoryItemsUseCase
import com.ivy.domain.usecase.transaction.CalculateTransactionsIncomeExpenseUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsByIdsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

private val AccountTransfersCategoryColorArgb = 0xFFFFCCD5.toInt()

@Stable
@HiltViewModel
internal class TransactionsViewModel @Inject internal constructor(
    private val periodState: PeriodState,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val updateAccountWithBalanceUseCase: UpdateAccountWithBalanceUseCase,
    private val payOrSkipPlannedTransactionByIdUseCase: PayOrSkipPlannedTransactionByIdUseCase,
    private val payOrSkipPlannedTransactionsByIdsUseCase: PayOrSkipPlannedTransactionsByIdsUseCase,
    private val getTransfersAsIncomeExpensePreference: GetTransfersAsIncomeExpensePreferenceUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getAccountTransactionsUseCase: GetAccountTransactionsUseCase,
    private val buildTransactionHistoryItemsUseCase: BuildTransactionHistoryItemsUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getAccountUpcomingTransactionsSummaryUseCase: GetAccountUpcomingTransactionsSummaryUseCase,
    private val getAccountOverdueTransactionsSummaryUseCase: GetAccountOverdueTransactionsSummaryUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getCategoryUseCase: GetCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val getCategoryTransactionsSummaryUseCase: GetCategoryTransactionsSummaryUseCase,
    private val getUnspecifiedCategoryTransactionsSummaryUseCase: GetUnspecifiedCategoryTransactionsSummaryUseCase,
    private val calculateAccountBalanceUseCase: CalculateAccountBalanceUseCase,
    private val calculateAccountIncomeExpenseUseCase: CalculateAccountIncomeExpenseUseCase,
    private val calculateTransactionsIncomeExpenseUseCase: CalculateTransactionsIncomeExpenseUseCase,
    private val getTransactionsByIdsUseCase: GetTransactionsByIdsUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val resourceProvider: ResourceProvider,
    private val preferenceToggleService: PreferenceToggleService,
    private val preferenceToggles: PreferenceToggleCatalog
) : ComposeViewModel<TransactionsState, TransactionsEvent>() {

    private val period = mutableStateOf(periodState.selectedPeriod)
    private val categories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private var loadedAccounts: ImmutableList<Account> = persistentListOf()
    private val accounts = mutableStateOf<ImmutableList<TransactionsListAccount>>(persistentListOf())
    private val baseCurrency = mutableStateOf("")
    private val currency = mutableStateOf("")
    private val balance = mutableDoubleStateOf(0.0)
    private val balanceBaseCurrency = mutableStateOf<Double?>(null)
    private val income = mutableDoubleStateOf(0.0)
    private val expenses = mutableDoubleStateOf(0.0)

    // Upcoming
    private val upcoming = mutableStateOf(
        TransactionsDueSection(
            transactions = persistentListOf(),
            expanded = false,
            income = 0.0,
            expenses = 0.0,
        )
    )

    // Overdue
    private val overdue = mutableStateOf(
        TransactionsDueSection(
            transactions = persistentListOf(),
            expanded = true,
            income = 0.0,
            expenses = 0.0,
        )
    )

    // History
    private val history =
        mutableStateOf<ImmutableList<TransactionHistoryItem>>(persistentListOf())

    // Account transaction filter (by category / tag)
    private var accountTransactions: List<Transaction> = emptyList()
    private var accountIncludeTransfers = false
    private val filterCategories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private val filterHasUncategorized = mutableStateOf(false)
    private val filterTags = mutableStateOf<ImmutableList<Tag>>(persistentListOf())
    private val selectedCategoryIds = mutableStateOf<Set<UUID>>(emptySet())
    private val uncategorizedSelected = mutableStateOf(false)
    private val selectedTagIds = mutableStateOf<Set<UUID>>(emptySet())

    private val accountId = mutableStateOf<UUID?>(null)
    private val category = mutableStateOf<Category?>(null)
    private val initWithTransactions = mutableStateOf(false)
    private val treatTransfersAsIncomeExpense = mutableStateOf(false)
    private val accountNameConfirmation = mutableStateOf(selectEndTextFieldValue(""))
    private val enableDeletionButton = mutableStateOf(false)
    private val skipAllModalVisible = mutableStateOf(false)
    private val deleteModal1Visible = mutableStateOf(false)
    private val _uiEvents = MutableSharedFlow<TransactionsUiEvent>()
    val uiEvents: SharedFlow<TransactionsUiEvent> = _uiEvents.asSharedFlow()
    private var currentQuery: TransactionsQuery? = null

    @Composable
    override fun uiState(): TransactionsState {
        return TransactionsState(
            period = getPeriod(),
            baseCurrency = getBaseCurrency(),
            currency = getCurrency(),
            categories = getCategories(),
            accounts = getAccounts(),
            account = getAccount(),
            category = getCategory(),
            balance = getBalance(),
            balanceBaseCurrency = getBalanceBaseCurrency(),
            income = getIncome(),
            expenses = getExpenses(),
            incomeTransactionCount = getIncomeTransactionCount(),
            expenseTransactionCount = getExpenseTransactionCount(),
            initWithTransactions = getInitWithTransactions(),
            treatTransfersAsIncomeExpense = getTreatTransfersAsIncomeExpense(),
            history = getHistory(),
            upcoming = getUpcoming(),
            overdue = getOverdue(),
            enableDeletionButton = getEnableDeletionButton(),
            skipAllModalVisible = getSkipAllModalVisible(),
            deleteModal1Visible = getDeleteModal1Visible(),
            showAccountColorsInTransactions = getShouldShowAccountSpecificColorInTransactions(),
            accountFilter = getAccountFilter()
        )
    }

    @Composable
    fun getShouldShowAccountSpecificColorInTransactions(): Boolean {
        val preference = preferenceToggles.showAccountColorsInTransactions
        return preferenceToggleService.enabledFlow(preference)
            .asEnabledState(preference.defaultValue)
    }

    @Composable
    private fun getPeriod(): TimePeriod {
        return period.value
    }

    @Composable
    private fun getBaseCurrency(): String {
        return baseCurrency.value
    }

    @Composable
    private fun getAccount(): TransactionsAccount? {
        return selectedAccount()?.toTransactionsAccount()
    }

    @Composable
    private fun getCurrency(): String {
        return currency.value
    }

    @Composable
    private fun getCategories(): ImmutableList<Category> {
        return categories.value
    }

    @Composable
    private fun getAccounts(): ImmutableList<TransactionsListAccount> {
        return accounts.value
    }

    @Composable
    private fun getCategory(): Category? {
        return category.value
    }

    @Composable
    private fun getBalance(): Double {
        return balance.doubleValue
    }

    @Composable
    private fun getBalanceBaseCurrency(): Double? {
        return balanceBaseCurrency.value
    }

    @Composable
    private fun getIncome(): Double {
        return income.doubleValue
    }

    @Composable
    private fun getExpenses(): Double {
        return expenses.doubleValue
    }

    @Composable
    private fun getIncomeTransactionCount(): Int {
        return history.value.countTransactionType(TransactionType.INCOME)
    }

    @Composable
    private fun getExpenseTransactionCount(): Int {
        return history.value.countTransactionType(TransactionType.EXPENSE)
    }

    @Composable
    private fun getInitWithTransactions(): Boolean {
        return initWithTransactions.value
    }

    @Composable
    private fun getTreatTransfersAsIncomeExpense(): Boolean {
        return treatTransfersAsIncomeExpense.value
    }

    @Composable
    private fun getUpcoming(): TransactionsDueSection {
        return upcoming.value
    }

    @Composable
    private fun getHistory(): ImmutableList<TransactionHistoryItem> {
        return history.value
    }

    @Composable
    private fun getAccountFilter(): AccountTransactionFilter? {
        if (accountId.value == null) return null
        val availableCategories = filterCategories.value
        val availableTags = filterTags.value
        val hasUncategorized = filterHasUncategorized.value
        if (availableCategories.isEmpty() && availableTags.isEmpty() && !hasUncategorized) {
            return null
        }
        return AccountTransactionFilter(
            availableCategories = availableCategories,
            hasUncategorized = hasUncategorized,
            availableTags = availableTags,
            selectedCategoryIds = selectedCategoryIds.value.toImmutableSet(),
            uncategorizedSelected = uncategorizedSelected.value,
            selectedTagIds = selectedTagIds.value.toImmutableSet(),
        )
    }

    @Composable
    private fun getOverdue(): TransactionsDueSection {
        return overdue.value
    }

    @Composable
    private fun getEnableDeletionButton(): Boolean {
        return enableDeletionButton.value
    }

    @Composable
    private fun getSkipAllModalVisible(): Boolean {
        return skipAllModalVisible.value
    }

    @Composable
    private fun getDeleteModal1Visible(): Boolean {
        return deleteModal1Visible.value
    }

    override fun onEvent(event: TransactionsEvent) {
        when (event) {
            TransactionsEvent.Delete -> delete()
            is TransactionsEvent.EditAccount -> editAccount(
                event.accountId,
                event.data
            )

            is TransactionsEvent.EditCategory -> editCategory(event.updatedCategory)
            TransactionsEvent.NextMonth -> nextMonth()
            is TransactionsEvent.PayOrGet -> payOrGet(event.transactionId)
            TransactionsEvent.PreviousMonth -> previousMonth()
            is TransactionsEvent.SetPeriod -> setPeriod(event.period)
            is TransactionsEvent.SkipTransaction -> skipTransaction(event.transactionId)
            is TransactionsEvent.SkipTransactions -> skipTransactions(
                event.transactionIds
            )

            is TransactionsEvent.UpdateAccountDeletionState -> updateAccountDeletionState(
                event.confirmationText
            )

            is TransactionsEvent.SetOverdueExpanded -> setOverdueExpanded(event.expanded)
            is TransactionsEvent.SetUpcomingExpanded -> setUpcomingExpanded(event.expanded)
            is TransactionsEvent.SetSkipAllModalVisible -> setSkipAllModalVisible(event.visible)
            is TransactionsEvent.OnDeleteModal1Visible -> setDeleteModal1Visible(event.delete)
            is TransactionsEvent.ToggleCategoryFilter -> toggleCategoryFilter(event.categoryId)
            TransactionsEvent.ToggleUncategorizedFilter -> toggleUncategorizedFilter()
            is TransactionsEvent.ToggleTagFilter -> toggleTagFilter(event.tagId)
            TransactionsEvent.ClearFilters -> clearFilters()
        }
    }

    private suspend fun initForAccount(accountId: UUID) {
        this.accountId.value = accountId
        val initialAccount = selectedAccount() ?: error("account not found")
        val range = periodState.rangeOf(period.value)

        currency.value = initialAccount.asset.code

        val balanceValue = calculateAccountBalanceUseCase(initialAccount).toDouble()
        balance.doubleValue = balanceValue
        if (baseCurrency.value != currency.value) {
            balanceBaseCurrency.value = exchangeAmountUseCase(
                amount = balanceValue.toBigDecimal(),
                baseCurrency = baseCurrency.value,
                fromCurrency = currency.value
            ).getOrNull()?.toDouble()
        }

        val includeTransfersInCalc = getTransfersAsIncomeExpensePreference()

        accountIncludeTransfers = includeTransfersInCalc
        val accountTransactionList = getAccountTransactionsUseCase(
            accountId = initialAccount.id,
            range = range.toCloseTimeRange()
        )
        accountTransactions = accountTransactionList
        resetAccountFilterSelection()

        val incomeExpensePair = calculateAccountIncomeExpenseUseCase(
            account = initialAccount,
            transactions = accountTransactionList,
            includeTransfersInCalc = includeTransfersInCalc
        )
        income.doubleValue = incomeExpensePair.income.toDouble()
        expenses.doubleValue = incomeExpensePair.expense.toDouble()

        val accountHistory = buildTransactionHistoryItemsUseCase(
            baseCurrency = baseCurrency.value,
            transactions = accountTransactionList
        ).toImmutableList()
        history.value = accountHistory

        buildAccountFilterOptions(accountTransactionList, accountHistory)

        val upcomingSummary = withContext(Dispatchers.IO) {
            getAccountUpcomingTransactionsSummaryUseCase(initialAccount.id, range)
        }
        upcoming.value = upcoming.value.copy(
            transactions = upcomingSummary.transactions.toImmutableList(),
            income = upcomingSummary.income,
            expenses = upcomingSummary.expenses,
        )

        val overdueSummary = withContext(Dispatchers.IO) {
            getAccountOverdueTransactionsSummaryUseCase(initialAccount.id, range)
        }
        overdue.value = overdue.value.copy(
            transactions = overdueSummary.transactions.toImmutableList(),
            income = overdueSummary.income,
            expenses = overdueSummary.expenses,
        )
    }

    private suspend fun initForCategory(categoryId: UUID, accountFilterList: List<UUID>) {
        val accountFilterSet = accountFilterList.toSet()
        val initialCategory = withContext(Dispatchers.IO) {
            getCategoryUseCase(CategoryId(categoryId)) ?: error("category not found")
        }
        category.value = initialCategory
        val range = periodState.rangeOf(period.value)

        val summary = withContext(Dispatchers.IO) {
            getCategoryTransactionsSummaryUseCase(
                category = initialCategory,
                range = range,
                accountFilterSet = accountFilterSet
            )
        }
        applyCategorySummary(summary)
    }

    private suspend fun initForCategoryWithTransactions(
        categoryId: UUID,
        accountFilterList: List<UUID>,
        transactions: List<Transaction>,
    ) {
        withContext(Dispatchers.Default) {
            initWithTransactions.value = true

            val accountFilterSet = accountFilterList.toSet()
            val initialCategory = withContext(Dispatchers.IO) {
                getCategoryUseCase(CategoryId(categoryId)) ?: error("category not found")
            }
            category.value = initialCategory
            val range = periodState.rangeOf(period.value)

            val summary = withContext(Dispatchers.IO) {
                getCategoryTransactionsSummaryUseCase(
                    category = initialCategory,
                    range = range,
                    accountFilterSet = accountFilterSet,
                    providedTransactions = transactions
                )
            }
            applyCategorySummary(summary)
        }
    }

    private suspend fun initForUnspecifiedCategory() {
        val range = periodState.rangeOf(period.value)

        val summary = withContext(Dispatchers.IO) {
            getUnspecifiedCategoryTransactionsSummaryUseCase(range)
        }
        applyCategorySummary(summary)
    }

    private fun applyCategorySummary(summary: CategoryTransactionsSummary) {
        balance.doubleValue = summary.balance
        income.doubleValue = summary.income
        expenses.doubleValue = summary.expenses
        history.value = summary.history.toImmutableList()
        upcoming.value = upcoming.value.copy(
            transactions = summary.upcoming.transactions.toImmutableList(),
            income = summary.upcoming.income,
            expenses = summary.upcoming.expenses,
        )
        overdue.value = overdue.value.copy(
            transactions = summary.overdue.transactions.toImmutableList(),
            income = summary.overdue.income,
            expenses = summary.overdue.expenses,
        )
    }

    private suspend fun initForAccountTransfersCategory(
        accountFilterList: List<UUID>,
        transactions: List<Transaction>,
    ) {
        initWithTransactions.value = true
        val accountTransferCategory = Category(
            name = NotBlankTrimmedString.unsafe(resourceProvider.getString(R.string.account_transfers)),
            color = ColorInt(AccountTransfersCategoryColorArgb),
            icon = IconAsset.unsafe("transfer"),
            id = CategoryId(UUID.randomUUID()),
            orderNum = 0.0,
        )
        category.value = accountTransferCategory
        val accountFilterIdSet = accountFilterList.toHashSet()
        val filteredTransactions = transactions.filter {
            it.category == null && (
                    accountFilterIdSet.contains(it.getFromAccount().value) ||
                            it.getToAccount()?.value in accountFilterIdSet
                    )
                    && it.getTransactionType() == TransactionType.TRANSFER
        }

        val historyIncomeExpense = calculateTransactionsIncomeExpenseUseCase(
            transactions = filteredTransactions,
            accounts = accountFilterList.mapNotNull { accountId ->
                loadedAccounts.find { it.id.value == accountId }
            },
            baseCurrency = baseCurrency.value
        )

        income.doubleValue = historyIncomeExpense.transferIncome.toDouble()
        expenses.doubleValue = historyIncomeExpense.transferExpense.toDouble()
        balance.doubleValue = income.doubleValue - expenses.doubleValue
        history.value = buildTransactionHistoryItemsUseCase(
            baseCurrency = baseCurrency.value,
            transactions = transactions
        ).toImmutableList()
    }

    private fun reset() {
        accountId.value = null
        category.value = null
        accountTransactions = emptyList()
        accountIncludeTransfers = false
        filterCategories.value = persistentListOf()
        filterTags.value = persistentListOf()
        filterHasUncategorized.value = false
        resetAccountFilterSelection()
    }

    private fun setUpcomingExpanded(expanded: Boolean) {
        upcoming.value = upcoming.value.copy(expanded = expanded)
    }

    private fun setOverdueExpanded(expanded: Boolean) {
        overdue.value = overdue.value.copy(expanded = expanded)
    }

    private fun setPeriod(period: TimePeriod) {
        restartCurrentScreen(timePeriod = period)
    }

    private fun setSkipAllModalVisible(visible: Boolean) {
        skipAllModalVisible.value = visible
    }

    private fun nextMonth() {
        val nextPeriod = periodState.shiftMonth(period.value, increment = 1L)
        if (nextPeriod != null) {
            periodState.select(nextPeriod)
            restartCurrentScreen(timePeriod = nextPeriod)
        }
    }

    private fun previousMonth() {
        val previousPeriod = periodState.shiftMonth(period.value, increment = -1L)
        if (previousPeriod != null) {
            periodState.select(previousPeriod)
            restartCurrentScreen(timePeriod = previousPeriod)
        }
    }

    private fun delete() {
        viewModelScope.launch {
            val query = currentQuery ?: return@launch
            when {
                query.accountId != null -> {
                    deleteAccount(query.accountId)
                }

                query.categoryId != null -> {
                    deleteCategory(query.categoryId)
                }
            }
        }
    }

    private suspend fun deleteAccount(accountId: UUID) {
        withContext(Dispatchers.IO) {
            deleteAccountUseCase(AccountId(accountId))
        }
        _uiEvents.emit(TransactionsUiEvent.CloseScreen)
    }

    private suspend fun deleteCategory(categoryId: UUID) {
        withContext(Dispatchers.IO) {
            deleteCategoryUseCase(CategoryId(categoryId))
        }
        _uiEvents.emit(TransactionsUiEvent.CloseScreen)
    }

    private fun setDeleteModal1Visible(delete: Boolean) {
        deleteModal1Visible.value = delete
    }

    private fun editCategory(updatedCategory: Category) {
        viewModelScope.launch {
            if (updateCategoryUseCase(updatedCategory)) {
                category.value = updatedCategory
            }
        }
    }

    private fun editAccount(accountId: UUID, data: AccountModalSaveData) {
        val account = loadedAccounts.firstOrNull { it.id.value == accountId } ?: return

        val updatedAccount = account.copy(
            name = NotBlankTrimmedString.from(data.name).getOrNull() ?: account.name,
            asset = AssetCode.from(data.currency).getOrNull() ?: account.asset,
            color = ColorInt(data.color),
            icon = data.icon?.let(IconAsset::from)?.getOrNull(),
            includeInBalance = data.includeInBalance,
            visibleCategories = data.visibleCategoryIds
                ?.map { CategoryId(it) }
                ?: account.visibleCategories,
        )

        viewModelScope.launch {
            updateAccountWithBalanceUseCase(updatedAccount, data.balance)
            restartCurrentScreen(timePeriod = period.value)
        }
    }

    private fun payOrGet(transactionId: UUID) {
        viewModelScope.launch {
            if (payOrSkipPlannedTransactionByIdUseCase(transactionId)) {
                restartCurrentScreen()
            }
        }
    }

    private fun skipTransaction(transactionId: UUID) {
        viewModelScope.launch {
            val paidTransaction = payOrSkipPlannedTransactionByIdUseCase(
                transactionId = transactionId,
                skipTransaction = true
            )
            if (paidTransaction) {
                restartCurrentScreen()
            }
        }
    }

    private fun skipTransactions(transactionIds: List<UUID>) {
        viewModelScope.launch {
            val paidTransactions = payOrSkipPlannedTransactionsByIdsUseCase(
                transactionIds = transactionIds,
                skipTransaction = true
            )
            if (paidTransactions > 0) {
                restartCurrentScreen()
            }
        }
    }

    private fun updateAccountDeletionState(confirmationText: String) {
        accountNameConfirmation.value = selectEndTextFieldValue(confirmationText)
        enableDeletionButton.value = selectedAccount()?.name?.value == confirmationText ||
                category.value?.name?.value == confirmationText
    }

    fun start(
        query: TransactionsQuery,
        timePeriod: TimePeriod? = periodState.selectedPeriod,
        reset: Boolean = true,
    ) {
        currentQuery = query

        if (reset) {
            reset()
        }

        viewModelScope.launch {
            period.value = timePeriod ?: periodState.selectedPeriod

            val baseCurrencyValue = getBaseCurrencyCode()
            baseCurrency.value = baseCurrencyValue
            currency.value = baseCurrency.value

            categories.value = getCategoriesUseCase().toImmutableList()
            loadedAccounts = getAccountsUseCase().toImmutableList()
            accounts.value = loadedAccounts.map { it.toTransactionsListAccount() }.toImmutableList()
            initWithTransactions.value = false
            treatTransfersAsIncomeExpense.value =
                getTransfersAsIncomeExpensePreference()
            val inputTransactions = getTransactionsByIdsUseCase(query.transactionIds)

            when {
                query.accountId != null -> {
                    initForAccount(query.accountId)
                }

                query.categoryId != null && inputTransactions.isEmpty() -> {
                    initForCategory(query.categoryId, query.accountIdFilterList)
                }
                // Reports use a synthetic account-transfers category; keep it separate from
                // the real unspecified-category branch.
                query.categoryId != null && inputTransactions.isNotEmpty() &&
                        !query.unspecifiedCategory -> {
                    initForCategoryWithTransactions(
                        query.categoryId,
                        query.accountIdFilterList,
                        inputTransactions
                    )
                }

                query.unspecifiedCategory && inputTransactions.isNotEmpty() -> {
                    initForAccountTransfersCategory(
                        query.accountIdFilterList,
                        inputTransactions
                    )
                }

                query.unspecifiedCategory -> {
                    initForUnspecifiedCategory()
                }

                else -> error("no id provided")
            }
        }
    }

    private fun restartCurrentScreen(
        timePeriod: TimePeriod? = period.value,
        reset: Boolean = false,
    ) {
        currentQuery?.let {
            start(
                query = it,
                timePeriod = timePeriod,
                reset = reset
            )
        }
    }

    private fun toggleCategoryFilter(categoryId: UUID) {
        selectedCategoryIds.value = selectedCategoryIds.value.toggleElement(categoryId)
        applyAccountFilter()
    }

    private fun toggleUncategorizedFilter() {
        uncategorizedSelected.value = !uncategorizedSelected.value
        applyAccountFilter()
    }

    private fun toggleTagFilter(tagId: UUID) {
        selectedTagIds.value = selectedTagIds.value.toggleElement(tagId)
        applyAccountFilter()
    }

    private fun clearFilters() {
        resetAccountFilterSelection()
        applyAccountFilter()
    }

    private fun resetAccountFilterSelection() {
        selectedCategoryIds.value = emptySet()
        uncategorizedSelected.value = false
        selectedTagIds.value = emptySet()
    }

    private fun buildAccountFilterOptions(
        transactions: List<Transaction>,
        historyItems: List<TransactionHistoryItem>,
    ) {
        val usedCategoryIds = transactions.mapNotNull { it.category?.value }.toHashSet()
        filterCategories.value = categories.value
            .filter { it.id.value in usedCategoryIds }
            .toImmutableList()
        filterHasUncategorized.value = transactions.any { it.category == null }

        val tagsById = LinkedHashMap<UUID, Tag>()
        historyItems.forEach { item ->
            if (item is TransactionHistoryTransaction) {
                item.tags.forEach { tag -> tagsById.putIfAbsent(tag.id.value, tag) }
            }
        }
        filterTags.value = tagsById.values.toImmutableList()
    }

    private fun applyAccountFilter() {
        val account = selectedAccount() ?: return
        viewModelScope.launch {
            val filtered = filterAccountTransactions(accountTransactions)
            val incomeExpensePair = calculateAccountIncomeExpenseUseCase(
                account = account,
                transactions = filtered,
                includeTransfersInCalc = accountIncludeTransfers
            )
            income.doubleValue = incomeExpensePair.income.toDouble()
            expenses.doubleValue = incomeExpensePair.expense.toDouble()
            history.value = buildTransactionHistoryItemsUseCase(
                baseCurrency = baseCurrency.value,
                transactions = filtered
            ).toImmutableList()
        }
    }

    private fun filterAccountTransactions(
        transactions: List<Transaction>
    ): List<Transaction> {
        val categoryIds = selectedCategoryIds.value
        val includeUncategorized = uncategorizedSelected.value
        val tagIds = selectedTagIds.value
        val categoryFilterActive = categoryIds.isNotEmpty() || includeUncategorized
        val tagFilterActive = tagIds.isNotEmpty()
        if (!categoryFilterActive && !tagFilterActive) return transactions
        return transactions.filter { transaction ->
            val categoryMatches = !categoryFilterActive || run {
                val txCategoryId = transaction.category?.value
                if (txCategoryId == null) includeUncategorized else txCategoryId in categoryIds
            }
            val tagMatches = !tagFilterActive ||
                    transaction.tags.any { it.value in tagIds }
            categoryMatches && tagMatches
        }
    }

    private fun selectedAccount(): Account? {
        val id = accountId.value ?: return null
        return loadedAccounts.firstOrNull { it.id.value == id }
    }
}

private fun <T> Set<T>.toggleElement(element: T): Set<T> =
    if (contains(element)) this - element else this + element

private fun List<TransactionHistoryItem>.countTransactionType(type: TransactionType): Int {
    return filterIsInstance<TransactionHistoryTransaction>()
        .count { it.transaction.getTransactionType() == type }
}

private fun Account.toTransactionsListAccount() = TransactionsListAccount(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
    currency = asset.code,
)

internal data class TransactionsQuery(
    val accountId: UUID?,
    val categoryId: UUID?,
    val unspecifiedCategory: Boolean,
    val accountIdFilterList: ImmutableList<UUID>,
    val transactionIds: ImmutableList<UUID>,
)

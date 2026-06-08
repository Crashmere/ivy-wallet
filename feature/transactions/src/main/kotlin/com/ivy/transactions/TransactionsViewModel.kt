package com.ivy.transactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.TransactionType
import com.ivy.ui.resource.ResourceProvider
import com.ivy.data.model.AccountId
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.usecase.account.DeleteAccountUseCase
import com.ivy.domain.usecase.account.GetAccountUseCase
import com.ivy.domain.usecase.category.DeleteCategoryUseCase
import com.ivy.domain.usecase.category.CategoryTransactionsSummary
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.category.GetCategoryUseCase
import com.ivy.domain.usecase.category.GetCategoryTransactionsSummaryUseCase
import com.ivy.domain.usecase.category.GetUnspecifiedCategoryTransactionsSummaryUseCase
import com.ivy.domain.usecase.category.UpdateCategoryUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.usecase.transaction.MapTransactionsToLegacyTransactionsUseCase
import com.ivy.domain.usecase.transaction.MapTransactionsToLegacyTransactionsWithTagsUseCase
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggles
import com.ivy.ui.period.PeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.data.model.toCloseTimeRange
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.R
import com.ivy.ui.preferences.asEnabledState
import com.ivy.domain.usecase.account.CalculateAccountBalanceUseCase
import com.ivy.domain.usecase.account.CalculateAccountIncomeExpenseUseCase
import com.ivy.domain.usecase.account.GetAccountTransactionsUseCase
import com.ivy.domain.usecase.account.GetLegacyAccountUseCase
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.domain.usecase.account.GetAccountOverdueTransactionsSummaryUseCase
import com.ivy.domain.usecase.account.GetAccountUpcomingTransactionsSummaryUseCase
import com.ivy.domain.usecase.account.UpdateAccountWithBalanceUseCase
import com.ivy.domain.usecase.planned.PayOrSkipLegacyPlannedTransactionUseCase
import com.ivy.domain.usecase.planned.PayOrSkipLegacyPlannedTransactionsUseCase
import com.ivy.domain.usecase.settings.GetTransfersAsIncomeExpensePreferenceUseCase
import com.ivy.domain.usecase.transaction.BuildLegacyTransactionHistoryItemsUseCase
import com.ivy.domain.usecase.transaction.CalculateLegacyTransactionsIncomeExpenseUseCase
import com.ivy.domain.usecase.transaction.GetLegacyTransactionsByIdsUseCase
import com.ivy.domain.exchange.ExchangeData
import com.ivy.ui.modal.ChoosePeriodModalData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import com.ivy.data.model.legacy.LegacyAccount

private val AccountTransfersCategoryColorArgb = 0xFFFFCCD5.toInt()

@Stable
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val periodState: PeriodState,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val updateAccountWithBalanceUseCase: UpdateAccountWithBalanceUseCase,
    private val payOrSkipLegacyPlannedTransactionUseCase: PayOrSkipLegacyPlannedTransactionUseCase,
    private val payOrSkipLegacyPlannedTransactionsUseCase: PayOrSkipLegacyPlannedTransactionsUseCase,
    private val getTransfersAsIncomeExpensePreference: GetTransfersAsIncomeExpensePreferenceUseCase,
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val getLegacyAccountUseCase: GetLegacyAccountUseCase,
    private val getAccountTransactionsUseCase: GetAccountTransactionsUseCase,
    private val buildLegacyTransactionHistoryItemsUseCase: BuildLegacyTransactionHistoryItemsUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getAccountUpcomingTransactionsSummaryUseCase: GetAccountUpcomingTransactionsSummaryUseCase,
    private val getAccountOverdueTransactionsSummaryUseCase: GetAccountOverdueTransactionsSummaryUseCase,
    private val getAccountUseCase: GetAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getCategoryUseCase: GetCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val getCategoryTransactionsSummaryUseCase: GetCategoryTransactionsSummaryUseCase,
    private val getUnspecifiedCategoryTransactionsSummaryUseCase: GetUnspecifiedCategoryTransactionsSummaryUseCase,
    private val calculateAccountBalanceUseCase: CalculateAccountBalanceUseCase,
    private val calculateAccountIncomeExpenseUseCase: CalculateAccountIncomeExpenseUseCase,
    private val calculateLegacyTransactionsIncomeExpenseUseCase: CalculateLegacyTransactionsIncomeExpenseUseCase,
    private val getLegacyTransactionsByIdsUseCase: GetLegacyTransactionsByIdsUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val mapTransactionsToLegacyTransactionsUseCase: MapTransactionsToLegacyTransactionsUseCase,
    private val mapTransactionsToLegacyTransactionsWithTagsUseCase: MapTransactionsToLegacyTransactionsWithTagsUseCase,
    private val resourceProvider: ResourceProvider,
    private val preferenceToggleService: PreferenceToggleService,
    private val preferenceToggles: PreferenceToggles
) : ComposeViewModel<TransactionsState, TransactionsEvent>() {

    private val period = mutableStateOf(periodState.selectedPeriod)
    private val categories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private val accounts = mutableStateOf<ImmutableList<LegacyAccount>>(persistentListOf())
    private val baseCurrency = mutableStateOf("")
    private val currency = mutableStateOf("")
    private val balance = mutableDoubleStateOf(0.0)
    private val balanceBaseCurrency = mutableStateOf<Double?>(null)
    private val income = mutableDoubleStateOf(0.0)
    private val expenses = mutableDoubleStateOf(0.0)

    // Upcoming
    private val upcoming = mutableStateOf<ImmutableList<LegacyTransaction>>(persistentListOf())
    private val upcomingIncome = mutableDoubleStateOf(0.0)
    private val upcomingExpenses = mutableDoubleStateOf(0.0)
    private val upcomingExpanded = mutableStateOf(false)

    // Overdue
    private val overdue = mutableStateOf<ImmutableList<LegacyTransaction>>(persistentListOf())
    private val overdueIncome = mutableDoubleStateOf(0.0)
    private val overdueExpenses = mutableDoubleStateOf(0.0)
    private val overdueExpanded = mutableStateOf(true)

    // History
    private val history =
        mutableStateOf<ImmutableList<TransactionHistoryItem>>(persistentListOf())

    private val account = mutableStateOf<LegacyAccount?>(null)
    private val category = mutableStateOf<Category?>(null)
    private val initWithTransactions = mutableStateOf(false)
    private val treatTransfersAsIncomeExpense = mutableStateOf(false)
    private val accountNameConfirmation = mutableStateOf(selectEndTextFieldValue(""))
    private val enableDeletionButton = mutableStateOf(false)
    private val skipAllModalVisible = mutableStateOf(false)
    private val deleteModal1Visible = mutableStateOf(false)
    private val choosePeriodModal = mutableStateOf<ChoosePeriodModalData?>(null)
    private val _uiEvents = MutableSharedFlow<TransactionsUiEvent>()
    val uiEvents: SharedFlow<TransactionsUiEvent> = _uiEvents.asSharedFlow()

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
            initWithTransactions = getInitWithTransactions(),
            treatTransfersAsIncomeExpense = getTreatTransfersAsIncomeExpense(),
            history = getHistory(),
            upcoming = getUpcoming(),
            upcomingExpanded = getUpcomingExpanded(),
            upcomingIncome = getUpcomingIncome(),
            upcomingExpenses = getUpcomingExpenses(),
            overdue = getOverdue(),
            overdueExpanded = getOverdueExpanded(),
            overdueIncome = getOverdueIncome(),
            overdueExpenses = getOverdueExpenses(),
            enableDeletionButton = getEnableDeletionButton(),
            skipAllModalVisible = getSkipAllModalVisible(),
            deleteModal1Visible = getDeleteModal1Visible(),
            choosePeriodModal = getChoosePeriodModal(),
            showAccountColorsInTransactions = getShouldShowAccountSpecificColorInTransactions()
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
    private fun getAccount(): LegacyAccount? {
        return account.value
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
    private fun getAccounts(): ImmutableList<LegacyAccount> {
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
    private fun getInitWithTransactions(): Boolean {
        return initWithTransactions.value
    }

    @Composable
    private fun getTreatTransfersAsIncomeExpense(): Boolean {
        return treatTransfersAsIncomeExpense.value
    }

    @Composable
    private fun getUpcomingExpenses(): Double {
        return upcomingExpenses.doubleValue
    }

    @Composable
    private fun getUpcoming(): ImmutableList<LegacyTransaction> {
        return upcoming.value
    }

    @Composable
    private fun getUpcomingExpanded(): Boolean {
        return upcomingExpanded.value
    }

    @Composable
    private fun getUpcomingIncome(): Double {
        return upcomingIncome.doubleValue
    }

    @Composable
    private fun getHistory(): ImmutableList<TransactionHistoryItem> {
        return history.value
    }

    @Composable
    private fun getOverdue(): ImmutableList<LegacyTransaction> {
        return overdue.value
    }

    @Composable
    private fun getOverdueExpanded(): Boolean {
        return overdueExpanded.value
    }

    @Composable
    private fun getOverdueIncome(): Double {
        return overdueIncome.doubleValue
    }

    @Composable
    private fun getOverdueExpenses(): Double {
        return overdueExpenses.doubleValue
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

    @Composable
    private fun getChoosePeriodModal(): ChoosePeriodModalData? {
        return choosePeriodModal.value
    }

    override fun onEvent(event: TransactionsEvent) {
        when (event) {
            is TransactionsEvent.Delete -> delete(event.screen)
            is TransactionsEvent.EditAccount -> editAccount(
                event.screen,
                event.account,
                event.newBalance
            )

            is TransactionsEvent.EditCategory -> editCategory(event.updatedCategory)
            is TransactionsEvent.NextMonth -> nextMonth(event.screen)
            is TransactionsEvent.PayOrGet -> payOrGet(event.screen, event.transaction)
            is TransactionsEvent.PreviousMonth -> previousMonth(event.screen)
            is TransactionsEvent.SetPeriod -> setPeriod(event.screen, event.period)
            is TransactionsEvent.SkipTransaction -> skipTransaction(event.screen, event.transaction)
            is TransactionsEvent.SkipTransactions -> skipTransactions(
                event.screen,
                event.transactions
            )

            is TransactionsEvent.UpdateAccountDeletionState -> updateAccountDeletionState(
                event.confirmationText
            )

            is TransactionsEvent.SetOverdueExpanded -> setOverdueExpanded(event.expanded)
            is TransactionsEvent.SetUpcomingExpanded -> setUpcomingExpanded(event.expanded)
            is TransactionsEvent.SetSkipAllModalVisible -> setSkipAllModalVisible(event.visible)
            is TransactionsEvent.OnDeleteModal1Visible -> setDeleteModal1Visible(event.delete)
            is TransactionsEvent.OnChoosePeriodModalData -> setChoosePeriodModalData(event.data)
        }
    }

    private suspend fun initForAccount(accountId: UUID) {
        val initialAccount = getLegacyAccountUseCase(accountId) ?: error("account not found")
        account.value = initialAccount
        val range = periodState.rangeOf(period.value)

        if (initialAccount.currency.isNullOrBlank().not()) {
            currency.value = initialAccount.currency!!
        }

        val account = getAccountUseCase(AccountId(accountId)) ?: error("account not found")

        val balanceValue = calculateAccountBalanceUseCase(account).toDouble()
        balance.doubleValue = balanceValue
        if (baseCurrency.value != currency.value) {
            balanceBaseCurrency.value = exchangeAmountUseCase(
                data = ExchangeData.fromCurrencyCode(
                    baseCurrency = baseCurrency.value,
                    fromCurrency = currency.value
                ),
                amount = balanceValue.toBigDecimal()
            ).getOrNull()?.toDouble()
        }

        val includeTransfersInCalc = getTransfersAsIncomeExpensePreference()

        val incomeExpensePair = calculateAccountIncomeExpenseUseCase(
            account = account,
            range = range.toCloseTimeRange(),
            includeTransfersInCalc = includeTransfersInCalc
        )
        income.doubleValue = incomeExpensePair.income.toDouble()
        expenses.doubleValue = incomeExpensePair.expense.toDouble()

        history.value = buildLegacyTransactionHistoryItemsUseCase(
            baseCurrency = baseCurrency.value,
            transactions = mapTransactionsToLegacyTransactionsWithTagsUseCase(
                getAccountTransactionsUseCase(
                    accountId = AccountId(initialAccount.id),
                    range = range.toCloseTimeRange()
                )
            )
        ).toImmutableList()

        val upcomingSummary = withContext(Dispatchers.IO) {
            getAccountUpcomingTransactionsSummaryUseCase(AccountId(initialAccount.id), range)
        }
        upcomingIncome.doubleValue = upcomingSummary.income
        upcomingExpenses.doubleValue = upcomingSummary.expenses
        upcoming.value = mapTransactionsToLegacyTransactionsUseCase(upcomingSummary.transactions)
            .toImmutableList()

        val overdueSummary = withContext(Dispatchers.IO) {
            getAccountOverdueTransactionsSummaryUseCase(AccountId(initialAccount.id), range)
        }
        overdueIncome.doubleValue = overdueSummary.income
        overdueExpenses.doubleValue = overdueSummary.expenses
        overdue.value = mapTransactionsToLegacyTransactionsUseCase(overdueSummary.transactions)
            .toImmutableList()
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
        transactions: List<LegacyTransaction>,
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
        upcomingIncome.doubleValue = summary.upcoming.income
        upcomingExpenses.doubleValue = summary.upcoming.expenses
        upcoming.value = summary.upcoming.transactions.toImmutableList()
        overdueIncome.doubleValue = summary.overdue.income
        overdueExpenses.doubleValue = summary.overdue.expenses
        overdue.value = summary.overdue.transactions.toImmutableList()
    }

    private suspend fun initForAccountTransfersCategory(
        accountFilterList: List<UUID>,
        transactions: List<LegacyTransaction>,
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
            it.categoryId == null && (
                    accountFilterIdSet.contains(it.accountId) || accountFilterIdSet.contains(
                        it.toAccountId
                    )
                    ) && it.type == TransactionType.TRANSFER
        }

        val historyIncomeExpense = calculateLegacyTransactionsIncomeExpenseUseCase(
            transactions = filteredTransactions,
            accounts = accountFilterList.mapNotNull { accID -> accounts.value.find { it.id == accID } },
            baseCurrency = baseCurrency.value
        )

        income.doubleValue = historyIncomeExpense.transferIncome.toDouble()
        expenses.doubleValue = historyIncomeExpense.transferExpense.toDouble()
        balance.doubleValue = income.doubleValue - expenses.doubleValue
        history.value = buildLegacyTransactionHistoryItemsUseCase(
            baseCurrency = baseCurrency.value,
            transactions = transactions
        ).toImmutableList()
    }

    private fun reset() {
        account.value = null
        category.value = null
    }

    private fun setUpcomingExpanded(expanded: Boolean) {
        upcomingExpanded.value = expanded
    }

    private fun setOverdueExpanded(expanded: Boolean) {
        overdueExpanded.value = expanded
    }

    private fun setPeriod(
        screen: TransactionsScreen,
        period: TimePeriod,
    ) {
        start(
            screen = screen,
            timePeriod = period,
            reset = false
        )
    }

    private fun setSkipAllModalVisible(visible: Boolean) {
        skipAllModalVisible.value = visible
    }

    private fun nextMonth(screen: TransactionsScreen) {
        val nextPeriod = periodState.shiftMonth(period.value, increment = 1L)
        if (nextPeriod != null) {
            periodState.select(nextPeriod)
            start(
                screen = screen,
                timePeriod = nextPeriod,
                reset = false
            )
        }
    }

    private fun previousMonth(screen: TransactionsScreen) {
        val previousPeriod = periodState.shiftMonth(period.value, increment = -1L)
        if (previousPeriod != null) {
            periodState.select(previousPeriod)
            start(
                screen = screen,
                timePeriod = previousPeriod,
                reset = false
            )
        }
    }

    private fun delete(screen: TransactionsScreen) {
        viewModelScope.launch {
            when {
                screen.accountId != null -> {
                    deleteAccount(screen.accountId!!)
                }

                screen.categoryId != null -> {
                    deleteCategory(screen.categoryId!!)
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

    private fun setChoosePeriodModalData(data: ChoosePeriodModalData?) {
        choosePeriodModal.value = data
    }

    private fun editCategory(updatedCategory: Category) {
        viewModelScope.launch {
            if (updateCategoryUseCase(updatedCategory)) {
                category.value = updatedCategory
            }
        }
    }

    private fun editAccount(
        screen: TransactionsScreen,
        account: LegacyAccount,
        newBalance: Double,
    ) {
        viewModelScope.launch {
            updateAccountWithBalanceUseCase(account, newBalance)
            start(
                screen = screen,
                timePeriod = period.value,
                reset = false
            )
        }
    }

    private fun payOrGet(screen: TransactionsScreen, transaction: LegacyTransaction) {
        viewModelScope.launch {
            if (payOrSkipLegacyPlannedTransactionUseCase(transaction) != null) {
                start(
                    screen = screen,
                    reset = false
                )
            }
        }
    }

    private fun skipTransaction(screen: TransactionsScreen, transaction: LegacyTransaction) {
        viewModelScope.launch {
            val paidTransaction = payOrSkipLegacyPlannedTransactionUseCase(
                transaction = transaction,
                skipTransaction = true
            )
            if (paidTransaction != null) {
                start(
                    screen = screen,
                    reset = false
                )
            }
        }
    }

    private fun skipTransactions(screen: TransactionsScreen, transactions: List<LegacyTransaction>) {
        viewModelScope.launch {
            val paidTransactions = payOrSkipLegacyPlannedTransactionsUseCase(
                transactions = transactions,
                skipTransaction = true
            )
            if (paidTransactions.isNotEmpty()) {
                start(
                    screen = screen,
                    reset = false
                )
            }
        }
    }

    private fun updateAccountDeletionState(confirmationText: String) {
        accountNameConfirmation.value = selectEndTextFieldValue(confirmationText)
        enableDeletionButton.value = account.value?.name == confirmationText ||
                category.value?.name?.value == confirmationText
    }

    fun start(
        screen: TransactionsScreen,
        timePeriod: TimePeriod? = periodState.selectedPeriod,
        reset: Boolean = true,
    ) {
        if (reset) {
            reset()
        }

        viewModelScope.launch {
            period.value = timePeriod ?: periodState.selectedPeriod

            val baseCurrencyValue = getBaseCurrencyCode()
            baseCurrency.value = baseCurrencyValue
            currency.value = baseCurrency.value

            categories.value = getCategoriesUseCase().toImmutableList()
            accounts.value = getLegacyAccountsUseCase()
            initWithTransactions.value = false
            treatTransfersAsIncomeExpense.value =
                getTransfersAsIncomeExpensePreference()
            val legacyTransactionsFromNavigation =
                getLegacyTransactionsByIdsUseCase(screen.legacyTransactionIds)

            when {
                screen.accountId != null -> {
                    initForAccount(screen.accountId!!)
                }

                screen.categoryId != null && legacyTransactionsFromNavigation.isEmpty() -> {
                    initForCategory(screen.categoryId!!, screen.accountIdFilterList)
                }
                // Reports use a synthetic account-transfers category; keep it separate from
                // the real unspecified-category branch.
                screen.categoryId != null && legacyTransactionsFromNavigation.isNotEmpty() &&
                        screen.unspecifiedCategory == false -> {
                    initForCategoryWithTransactions(
                        screen.categoryId!!,
                        screen.accountIdFilterList,
                        legacyTransactionsFromNavigation
                    )
                }

                screen.unspecifiedCategory == true && legacyTransactionsFromNavigation.isNotEmpty() -> {
                    initForAccountTransfersCategory(
                        screen.accountIdFilterList,
                        legacyTransactionsFromNavigation
                    )
                }

                screen.unspecifiedCategory == true -> {
                    initForUnspecifiedCategory()
                }

                else -> error("no id provided")
            }
        }
    }
}

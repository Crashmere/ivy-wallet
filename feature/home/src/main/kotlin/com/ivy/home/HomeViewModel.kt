package com.ivy.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.Account
import com.ivy.data.model.Theme
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.primitive.AssetCode
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.currency.SetBaseCurrencyUseCase
import com.ivy.domain.usecase.exchange.SyncExchangeRatesUseCase
import com.ivy.domain.usecase.transaction.HasTransactionsUseCase
import com.ivy.domain.usecase.transaction.MapTransactionsToLegacyTransactionsUseCase
import com.ivy.domain.usecase.settings.GetBufferAmountUseCase
import com.ivy.domain.usecase.settings.GetHideCurrentBalancePreferenceUseCase
import com.ivy.domain.usecase.settings.GetHideIncomePreferenceUseCase
import com.ivy.domain.usecase.settings.GetStartDayOfMonthUseCase
import com.ivy.domain.usecase.settings.GetThemeUseCase
import com.ivy.domain.usecase.settings.SetBufferAmountUseCase
import com.ivy.domain.usecase.settings.SwitchThemeUseCase
import com.ivy.domain.usecase.wallet.CalculateWalletBalanceUseCase
import com.ivy.domain.usecase.wallet.CalculateWalletIncomeExpenseUseCase
import com.ivy.home.customerjourney.CustomerJourneyCardModel
import com.ivy.home.customerjourney.CustomerJourneyCardsProvider
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.period.PeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.data.model.toUTCCloseTimeRange
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.preferences.asEnabledState
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.home.GetOverdueTransactionsInfoUseCase
import com.ivy.domain.usecase.home.GetUpcomingTransactionsInfoUseCase
import com.ivy.domain.usecase.planned.PayOrSkipPlannedTransactionByIdUseCase
import com.ivy.domain.usecase.planned.PayOrSkipPlannedTransactionsByIdsUseCase
import com.ivy.domain.usecase.transaction.GetTransactionHistoryItemsUseCase
import com.ivy.data.model.ClosedTimeRange
import com.ivy.data.model.IncomeExpensePair
import com.ivy.legacy.ui.transaction.TransactionListAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

@Stable
@HiltViewModel
internal class HomeViewModel @Inject internal constructor(
    private val themeState: ThemeState,
    private val payOrSkipPlannedTransactionByIdUseCase: PayOrSkipPlannedTransactionByIdUseCase,
    private val payOrSkipPlannedTransactionsByIdsUseCase: PayOrSkipPlannedTransactionsByIdsUseCase,
    private val customerJourneyCardsProvider: CustomerJourneyCardsProvider,
    private val getTransactionHistoryItemsUseCase: GetTransactionHistoryItemsUseCase,
    private val calculateWalletIncomeExpenseUseCase: CalculateWalletIncomeExpenseUseCase,
    private val calculateWalletBalanceUseCase: CalculateWalletBalanceUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val setBaseCurrency: SetBaseCurrencyUseCase,
    private val getThemeUseCase: GetThemeUseCase,
    private val switchThemeUseCase: SwitchThemeUseCase,
    private val getBufferAmountUseCase: GetBufferAmountUseCase,
    private val setBufferAmountUseCase: SetBufferAmountUseCase,
    private val getStartDayOfMonth: GetStartDayOfMonthUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getUpcomingTransactionsInfoUseCase: GetUpcomingTransactionsInfoUseCase,
    private val getOverdueTransactionsInfoUseCase: GetOverdueTransactionsInfoUseCase,
    private val getHideCurrentBalancePreference: GetHideCurrentBalancePreferenceUseCase,
    private val getHideIncomePreference: GetHideIncomePreferenceUseCase,
    private val syncExchangeRatesUseCase: SyncExchangeRatesUseCase,
    private val hasTransactionsUseCase: HasTransactionsUseCase,
    private val mapTransactionsToLegacyTransactionsUseCase: MapTransactionsToLegacyTransactionsUseCase,
    private val preferenceToggles: PreferenceToggleCatalog,
    private val preferenceToggleService: PreferenceToggleService,
    private val periodState: PeriodState
) : ComposeViewModel<HomeState, HomeEvent>() {
    private var currentTheme by mutableStateOf(Theme.AUTO)
    private var period by mutableStateOf(periodState.selectedPeriod)
    private var baseData by mutableStateOf(
        HomeTransactionListData(
            baseCurrency = "",
            accounts = persistentListOf(),
            categories = persistentListOf()
        )
    )
    private var history by mutableStateOf<ImmutableList<TransactionHistoryItem>>(persistentListOf())
    private var stats by mutableStateOf(IncomeExpensePair.zero())
    private var balance by mutableStateOf(BigDecimal.ZERO)
    private var buffer by mutableStateOf(
        BufferInfo(
            amount = BigDecimal.ZERO,
            bufferDiff = BigDecimal.ZERO,
        )
    )
    private var upcoming by mutableStateOf(
        HomeDueSection(
            transactions = persistentListOf(),
            stats = IncomeExpensePair.zero(),
            expanded = false,
        )
    )
    private var overdue by mutableStateOf(
        HomeDueSection(
            transactions = persistentListOf(),
            stats = IncomeExpensePair.zero(),
            expanded = false,
        )
    )
    private var customerJourneyCards by
    mutableStateOf<ImmutableList<CustomerJourneyCardModel>>(persistentListOf())
    private var hideBalance by mutableStateOf(false)
    private var hideIncome by mutableStateOf(false)
    private var expanded by mutableStateOf(true)
    private val _uiEvents = MutableSharedFlow<HomeUiEvent>()
    val uiEvents: SharedFlow<HomeUiEvent> = _uiEvents.asSharedFlow()

    private data class HomePreferences(
        val theme: Theme,
        val baseCurrency: String,
        val bufferAmount: BigDecimal,
    )

    private data class HomeRangeInput(
        val preferences: HomePreferences,
        val timeRange: ClosedTimeRange,
    )

    private data class HomeAccountsInput(
        val preferences: HomePreferences,
        val timeRange: ClosedTimeRange,
        val accounts: List<Account>,
    )

    private data class HomeBalanceInput(
        val preferences: HomePreferences,
        val timeRange: ClosedTimeRange,
        val balance: BigDecimal,
    )

    private data class HomeHistoryInput(
        val baseCurrency: String,
        val timeRange: ClosedTimeRange,
    )

    @Composable
    override fun uiState(): HomeState {
        LaunchedEffect(Unit) {
            start()
        }

        return HomeState(
            theme = getTheme(),
            period = getPeriod(),
            baseData = getBaseData(),
            history = getHistory(),
            stats = getStats(),
            balance = getBalance(),
            buffer = getBuffer(),
            upcoming = getUpcoming(),
            overdue = getOverdue(),
            customerJourneyCards = getCustomerJourneyCards(),
            hideBalance = getHideBalance(),
            expanded = getExpanded(),
            hideIncome = getHideIncome(),
            shouldShowAccountSpecificColorInTransactions = getShouldShowAccountSpecificColorInTransactions()
        )
    }

    @Composable
    fun getShouldShowAccountSpecificColorInTransactions(): Boolean {
        val preference = preferenceToggles.showAccountColorsInTransactions
        return preferenceToggleService.enabledFlow(preference)
            .asEnabledState(preference.defaultValue)
    }

    @Composable
    private fun getTheme(): Theme {
        return currentTheme
    }

    @Composable
    private fun getPeriod(): TimePeriod {
        return period
    }

    @Composable
    private fun getBaseData(): HomeTransactionListData {
        return baseData
    }

    @Composable
    private fun getHistory(): ImmutableList<TransactionHistoryItem> {
        return history
    }

    @Composable
    private fun getStats(): IncomeExpensePair {
        return stats
    }

    @Composable
    private fun getBalance(): BigDecimal {
        return balance
    }

    @Composable
    private fun getBuffer(): BufferInfo {
        return buffer
    }

    @Composable
    private fun getUpcoming(): HomeDueSection {
        return upcoming
    }

    @Composable
    private fun getOverdue(): HomeDueSection {
        return overdue
    }

    @Composable
    private fun getCustomerJourneyCards(): ImmutableList<CustomerJourneyCardModel> {
        return customerJourneyCards
    }

    @Composable
    private fun getHideBalance(): Boolean {
        return hideBalance
    }

    @Composable
    private fun getExpanded(): Boolean {
        return expanded
    }

    @Composable
    private fun getHideIncome(): Boolean {
        return hideIncome
    }

    override fun onEvent(event: HomeEvent) {
        viewModelScope.launch {
            when (event) {
                HomeEvent.BalanceClick -> onBalanceClick()
                HomeEvent.HiddenBalanceClick -> onHiddenBalanceClick()
                HomeEvent.HiddenIncomeClick -> onHiddenIncomeClick()
                is HomeEvent.PayOrGetPlanned -> payOrGetPlanned(event.transactionId)
                is HomeEvent.SkipPlanned -> skipPlanned(event.transactionId)
                is HomeEvent.SkipAllPlanned -> skipAllPlanned(event.transactionIds)
                is HomeEvent.SetPeriod -> setPeriod(event.period)
                HomeEvent.SelectNextMonth -> onSelectNextMonth()
                HomeEvent.SelectPreviousMonth -> onSelectPreviousMonth()
                is HomeEvent.SetUpcomingExpanded -> setUpcomingExpanded(event.expanded)
                is HomeEvent.SetOverdueExpanded -> setOverdueExpanded(event.expanded)
                is HomeEvent.SetBuffer -> setBuffer(event.buffer)
                is HomeEvent.SetCurrency -> setCurrency(event.currency)
                HomeEvent.SwitchTheme -> switchTheme()
                is HomeEvent.DismissCustomerJourneyCard -> dismissCustomerJourneyCard(event.card)
                is HomeEvent.SetExpanded -> setExpanded(event.expanded)
            }
        }
    }

    private suspend fun start() {
        val startDay = getStartDayOfMonth()
        periodState.updateStartDayOfMonth(startDay)
        periodState.initSelectedPeriod(
            startDayOfMonth = startDay
        )
        reload()
    }

    // -----------------------------------------------------------------------------------
    private suspend fun reload(
        timePeriod: TimePeriod = periodState.selectedPeriod
    ) {
        val preferences = loadHomePreferences()

        currentTheme = preferences.theme
        period = timePeriod
        hideBalance = getHideCurrentBalancePreference()
        hideIncome = getHideIncomePreference()

        // This restores the runtime theme when the user imports a local backup.
        themeState.update(theme = preferences.theme)

        val timeRange = periodState.rangeOf(period).toUTCCloseTimeRange()

        val transactionListInput = loadTransactionListData(
            HomeRangeInput(
                preferences = preferences,
                timeRange = timeRange
            )
        )
        val balanceInput = loadIncomeExpenseBalance(transactionListInput)
        val historyInput = loadBuffer(balanceInput)
        val dueInput = loadTransactionHistory(historyInput)
        loadDueTransactions(dueInput)
        loadCustomerJourney()
    }

    private suspend fun loadHomePreferences(): HomePreferences {
        return HomePreferences(
            theme = getThemeUseCase(),
            baseCurrency = getBaseCurrencyCode(),
            bufferAmount = getBufferAmountUseCase(),
        )
    }

    private suspend fun loadTransactionListData(
        input: HomeRangeInput
    ): HomeAccountsInput {
        val preferences = input.preferences
        val accounts = getAccountsUseCase()
        val categories = getCategoriesUseCase()

        baseData = HomeTransactionListData(
            baseCurrency = preferences.baseCurrency,
            categories = categories.toImmutableList(),
            accounts = accounts
                .map { it.toTransactionListAccount() }
                .toImmutableList()
        )

        return HomeAccountsInput(
            preferences = preferences,
            timeRange = input.timeRange,
            accounts = accounts
        )
    }

    private suspend fun loadIncomeExpenseBalance(
        input: HomeAccountsInput
    ): HomeBalanceInput {
        val preferences = input.preferences

        val incomeExpense = calculateWalletIncomeExpenseUseCase(
            baseCurrency = preferences.baseCurrency,
            accounts = input.accounts,
            range = input.timeRange
        )

        val balanceAmount = calculateWalletBalanceUseCase(
            baseCurrency = preferences.baseCurrency
        )

        balance = balanceAmount
        stats = incomeExpense

        return HomeBalanceInput(
            preferences = preferences,
            timeRange = input.timeRange,
            balance = balanceAmount
        )
    }

    private suspend fun loadBuffer(
        input: HomeBalanceInput
    ): HomeHistoryInput {
        val preferences = input.preferences

        buffer = BufferInfo(
            amount = preferences.bufferAmount,
            bufferDiff = input.balance - preferences.bufferAmount
        )

        return HomeHistoryInput(
            baseCurrency = preferences.baseCurrency,
            timeRange = input.timeRange
        )
    }

    private suspend fun loadTransactionHistory(
        input: HomeHistoryInput
    ): HomeHistoryInput {
        history = getTransactionHistoryItemsUseCase(
            range = input.timeRange,
            baseCurrency = input.baseCurrency
        )

        return input
    }

    private suspend fun loadDueTransactions(
        input: HomeHistoryInput
    ) {
        val upcomingResult = getUpcomingTransactionsInfoUseCase(
            baseCurrency = input.baseCurrency,
            range = input.timeRange
        )
        upcoming = HomeDueSection(
            transactions = mapTransactionsToLegacyTransactionsUseCase(upcomingResult.transactions).toImmutableList(),
            stats = upcomingResult.incomeExpense,
            expanded = upcoming.expanded
        )

        val overdueResult = getOverdueTransactionsInfoUseCase(
            baseCurrency = input.baseCurrency,
            toRange = input.timeRange.to
        )
        overdue = HomeDueSection(
            transactions = mapTransactionsToLegacyTransactionsUseCase(overdueResult.transactions).toImmutableList(),
            stats = overdueResult.incomeExpense,
            expanded = overdue.expanded
        )
    }

    private suspend fun loadCustomerJourney() {
        customerJourneyCards = withContext(Dispatchers.IO) {
            customerJourneyCardsProvider.loadCards().toImmutableList()
        }
    }
// -----------------------------------------------------------------

    private fun setUpcomingExpanded(expanded: Boolean) {
        upcoming = upcoming.copy(expanded = expanded)
    }

    private fun setOverdueExpanded(expanded: Boolean) {
        overdue = overdue.copy(expanded = expanded)
    }

    private suspend fun onBalanceClick() {
        val hasTransactions = hasTransactionsUseCase()
        if (hasTransactions) {
            _uiEvents.emit(HomeUiEvent.OpenBalance)
        } else {
            _uiEvents.emit(HomeUiEvent.OpenAccountsTab)
        }
    }

    private suspend fun onHiddenBalanceClick() {
        hideBalance = false

        // Showing Balance fow 5s
        delay(5000)

        hideBalance = true
    }

    private suspend fun onHiddenIncomeClick() {
        hideIncome = false

        // Showing Balance fow 5s
        delay(5000)

        hideIncome = true
    }

    private fun switchTheme() {
        viewModelScope.launch {
            val newTheme = switchThemeUseCase()
            themeState.update(newTheme)
            currentTheme = newTheme
        }
    }

    private fun setBuffer(newBuffer: Double) {
        viewModelScope.launch {
            val newAmount = setBufferAmountUseCase(newBuffer.toBigDecimal())
            buffer = buffer.copy(amount = newAmount)
        }
    }

    private suspend fun setCurrency(newCurrency: String) {
        val assetCode = AssetCode.from(newCurrency).getOrNull() ?: return
        setBaseCurrency(assetCode)
        syncExchangeRatesUseCase.sync(assetCode)
        reload()
    }

    private suspend fun payOrGetPlanned(transactionId: UUID) {
        val paidTransaction = payOrSkipPlannedTransactionByIdUseCase(
            transactionId = transactionId,
            skipTransaction = false
        )
        if (paidTransaction) {
            reload()
        }
    }

    private suspend fun skipPlanned(transactionId: UUID) {
        val paidTransaction = payOrSkipPlannedTransactionByIdUseCase(
            transactionId = transactionId,
            skipTransaction = true
        )
        if (paidTransaction) {
            reload()
        }
    }

    private suspend fun skipAllPlanned(transactionIds: List<UUID>) {
        val paidTransactions = payOrSkipPlannedTransactionsByIdsUseCase(
            transactionIds = transactionIds,
            skipTransaction = true
        )
        if (paidTransactions > 0) {
            reload()
        }
    }

    private suspend fun dismissCustomerJourneyCard(card: CustomerJourneyCardModel) {
        customerJourneyCardsProvider.dismissCard(card)
        reload()
    }

    private suspend fun onSelectNextMonth() {
        val period = periodState.shiftMonth(period, increment = 1L)
        if (period != null) {
            periodState.select(period)
            setPeriod(period)
        }
    }

    private suspend fun onSelectPreviousMonth() {
        val period = periodState.shiftMonth(period, increment = -1L)
        if (period != null) {
            periodState.select(period)
            setPeriod(period)
        }
    }

    private suspend fun setPeriod(period: TimePeriod) {
        reload(period)
    }

    @JvmName("setExpandedMethod")
    private fun setExpanded(expanded: Boolean) {
        this.expanded = expanded
    }
}

private fun Account.toTransactionListAccount() = TransactionListAccount(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
    currency = asset.code,
)

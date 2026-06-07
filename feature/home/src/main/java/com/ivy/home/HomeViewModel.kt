package com.ivy.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.base.theme.Theme
import com.ivy.data.model.legacy.Transaction
import com.ivy.data.model.legacy.TransactionHistoryItem
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.data.model.primitive.AssetCode
import com.ivy.domain.preferences.toggles.PreferenceToggleRepository
import com.ivy.domain.preferences.toggles.PreferenceToggles
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.currency.SetBaseCurrencyUseCase
import com.ivy.domain.usecase.exchange.SyncExchangeRatesUseCase
import com.ivy.domain.usecase.transaction.HasTransactionsUseCase
import com.ivy.domain.usecase.transaction.MapTransactionsToLegacyUseCase
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
import com.ivy.legacy.ui.state.PeriodState
import com.ivy.legacy.ui.model.AppBaseData
import com.ivy.legacy.ui.model.BufferInfo
import com.ivy.legacy.ui.model.LegacyDueSection
import com.ivy.legacy.ui.model.period.TimePeriod
import com.ivy.data.model.legacy.toUTCCloseTimeRange
import com.ivy.data.model.legacy.Account
import com.ivy.base.coroutines.ioThread
import com.ivy.ui.navigation.BalanceScreen
import com.ivy.ui.navigation.MainTab
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.MainTabState
import com.ivy.ui.navigation.Navigation
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.preferences.asEnabledState
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.domain.usecase.home.GetOverdueTransactionsInfoUseCase
import com.ivy.domain.usecase.home.GetUpcomingTransactionsInfoUseCase
import com.ivy.domain.usecase.planned.PayOrSkipLegacyPlannedTransactionUseCase
import com.ivy.domain.usecase.planned.PayOrSkipLegacyPlannedTransactionsUseCase
import com.ivy.domain.usecase.transaction.GetTransactionHistoryItemsUseCase
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.data.model.legacy.IncomeExpensePair
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.ZoneOffset
import javax.inject.Inject

@Stable
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val themeState: ThemeState,
    private val nav: Navigation,
    private val payOrSkipLegacyPlannedTransactionUseCase: PayOrSkipLegacyPlannedTransactionUseCase,
    private val payOrSkipLegacyPlannedTransactionsUseCase: PayOrSkipLegacyPlannedTransactionsUseCase,
    private val customerJourneyLogic: CustomerJourneyCardsProvider,
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
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getUpcomingTransactionsInfoUseCase: GetUpcomingTransactionsInfoUseCase,
    private val getOverdueTransactionsInfoUseCase: GetOverdueTransactionsInfoUseCase,
    private val getHideCurrentBalancePreference: GetHideCurrentBalancePreferenceUseCase,
    private val getHideIncomePreference: GetHideIncomePreferenceUseCase,
    private val syncExchangeRatesUseCase: SyncExchangeRatesUseCase,
    private val hasTransactionsUseCase: HasTransactionsUseCase,
    private val mapTransactionsToLegacyUseCase: MapTransactionsToLegacyUseCase,
    private val timeProvider: TimeProvider,
    private val timeConverter: TimeConverter,
    private val preferenceToggles: PreferenceToggles,
    private val preferenceToggleRepository: PreferenceToggleRepository,
    private val periodState: PeriodState,
    private val mainTabState: MainTabState
) : ComposeViewModel<HomeState, HomeEvent>() {
    private var currentTheme by mutableStateOf(Theme.AUTO)
    private var period by mutableStateOf(periodState.selectedPeriod)
    private var baseData by mutableStateOf(
        AppBaseData(
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
        LegacyDueSection(
            trns = persistentListOf(),
            stats = IncomeExpensePair.zero(),
            expanded = false,
        )
    )
    private var overdue by mutableStateOf(
        LegacyDueSection(
            trns = persistentListOf(),
            stats = IncomeExpensePair.zero(),
            expanded = false,
        )
    )
    private var customerJourneyCards by
    mutableStateOf<ImmutableList<CustomerJourneyCardModel>>(persistentListOf())
    private var hideBalance by mutableStateOf(false)
    private var hideIncome by mutableStateOf(false)
    private var expanded by mutableStateOf(true)

    private data class HomePreferences(
        val theme: Theme,
        val baseCurrency: String,
        val bufferAmount: BigDecimal,
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
        return preferenceToggleRepository.enabledFlow(preference)
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
    private fun getBaseData(): AppBaseData {
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
    private fun getUpcoming(): LegacyDueSection {
        return upcoming
    }

    @Composable
    private fun getOverdue(): LegacyDueSection {
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
                is HomeEvent.PayOrGetPlanned -> payOrGetPlanned(event.transaction)
                is HomeEvent.SkipPlanned -> skipPlanned(event.transaction)
                is HomeEvent.SkipAllPlanned -> skipAllPlanned(event.transactions)
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

        val timeRange = period.toRange(
            startDateOfMonth = periodState.startDayOfMonth,
            timeConverter = timeConverter,
            timeProvider = timeProvider
        ).toUTCCloseTimeRange()

        val appDataInput = loadAppBaseData(preferences to timeRange)
        val balanceInput = loadIncomeExpenseBalance(appDataInput)
        val historyInput = loadBuffer(balanceInput)
        val dueInput = loadTrnHistory(historyInput)
        loadDueTrns(dueInput)
        loadCustomerJourney(Unit)
    }

    private suspend fun loadHomePreferences(): HomePreferences {
        return HomePreferences(
            theme = getThemeUseCase(),
            baseCurrency = getBaseCurrencyCode(),
            bufferAmount = getBufferAmountUseCase(),
        )
    }

    private suspend fun loadAppBaseData(
        input: Pair<HomePreferences, ClosedTimeRange>
    ): Triple<HomePreferences, ClosedTimeRange, List<Account>> {
        val (preferences, timeRange) = input
        val accounts = getLegacyAccountsUseCase()
        val categories = getCategoriesUseCase()

        baseData = AppBaseData(
            baseCurrency = preferences.baseCurrency,
            categories = categories.toImmutableList(),
            accounts = accounts.toImmutableList()
        )

        return Triple(preferences, timeRange, accounts)
    }

    private suspend fun loadIncomeExpenseBalance(
        input: Triple<HomePreferences, ClosedTimeRange, List<Account>>
    ): Triple<HomePreferences, ClosedTimeRange, BigDecimal> {
        val (preferences, timeRange, accounts) = input

        val incomeExpense = calculateWalletIncomeExpenseUseCase(
            baseCurrency = preferences.baseCurrency,
            accounts = accounts,
            range = timeRange
        )

        val balanceAmount = calculateWalletBalanceUseCase(
            baseCurrency = preferences.baseCurrency
        )

        balance = balanceAmount
        stats = incomeExpense

        return Triple(preferences, timeRange, balanceAmount)
    }

    private suspend fun loadBuffer(
        input: Triple<HomePreferences, ClosedTimeRange, BigDecimal>
    ): Pair<String, ClosedTimeRange> {
        val (preferences, timeRange, balance) = input

        buffer = BufferInfo(
            amount = preferences.bufferAmount,
            bufferDiff = balance - preferences.bufferAmount
        )

        return preferences.baseCurrency to timeRange
    }

    private suspend fun loadTrnHistory(
        input: Pair<String, ClosedTimeRange>
    ): Pair<String, ClosedTimeRange> {
        val (baseCurrency, timeRange) = input

        history = getTransactionHistoryItemsUseCase(
            range = timeRange,
            baseCurrency = baseCurrency
        )

        return baseCurrency to timeRange
    }

    private suspend fun loadDueTrns(
        input: Pair<String, ClosedTimeRange>
    ) {
        val (baseCurrency, timeRange) = input
        val upcomingResult = getUpcomingTransactionsInfoUseCase(
            baseCurrency = baseCurrency,
            range = timeRange
        )
        upcoming = LegacyDueSection(
            trns = mapTransactionsToLegacyUseCase(upcomingResult.transactions).toImmutableList(),
            stats = upcomingResult.incomeExpense,
            expanded = upcoming.expanded
        )

        val overdueResult = getOverdueTransactionsInfoUseCase(
            baseCurrency = baseCurrency,
            toRange = timeRange.to
        )
        overdue = LegacyDueSection(
            trns = mapTransactionsToLegacyUseCase(overdueResult.transactions).toImmutableList(),
            stats = overdueResult.incomeExpense,
            expanded = overdue.expanded
        )
    }

    private suspend fun loadCustomerJourney(unit: Unit) {
        customerJourneyCards = ioThread {
            customerJourneyLogic.loadCards().toImmutableList()
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
            // has transactions show him "Balance" screen
            nav.navigateTo(BalanceScreen)
        } else {
            // doesn't have transactions lead him to adjust balance
            mainTabState.select(MainTab.ACCOUNTS)
            nav.navigateTo(MainScreen)
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

    private suspend fun payOrGetPlanned(transaction: Transaction) {
        val paidTransaction = payOrSkipLegacyPlannedTransactionUseCase(
            transaction = transaction,
            skipTransaction = false
        )
        if (paidTransaction != null) {
            reload()
        }
    }

    private suspend fun skipPlanned(transaction: Transaction) {
        val paidTransaction = payOrSkipLegacyPlannedTransactionUseCase(
            transaction = transaction,
            skipTransaction = true
        )
        if (paidTransaction != null) {
            reload()
        }
    }

    private suspend fun skipAllPlanned(transactions: List<Transaction>) {
        val paidTransactions = payOrSkipLegacyPlannedTransactionsUseCase(
            transactions = transactions,
            skipTransaction = true
        )
        if (paidTransactions.isNotEmpty()) {
            reload()
        }
    }

    private suspend fun dismissCustomerJourneyCard(card: CustomerJourneyCardModel) {
        customerJourneyLogic.dismissCard(card)
        reload()
    }

    private suspend fun onSelectNextMonth() {
        val month = period.month
        val year = period.year ?: currentUtcYear()
        val period = month?.incrementMonthPeriod(
            increment = 1L,
            year = year,
            referenceDate = timeProvider.localDateNow(),
        )
        if (period != null) {
            periodState.select(period)
            setPeriod(period)
        }
    }

    private suspend fun onSelectPreviousMonth() {
        val month = period.month
        val year = period.year ?: currentUtcYear()
        val period = month?.incrementMonthPeriod(
            increment = -1L,
            year = year,
            referenceDate = timeProvider.localDateNow(),
        )
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

    private fun currentUtcYear(): Int =
        timeProvider.utcNow().atZone(ZoneOffset.UTC).year
}

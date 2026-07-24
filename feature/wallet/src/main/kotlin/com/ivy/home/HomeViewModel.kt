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
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.transaction.HasTransactionsUseCase
import com.ivy.domain.usecase.settings.GetHideCurrentBalancePreferenceUseCase
import com.ivy.domain.usecase.settings.GetHideIncomePreferenceUseCase
import com.ivy.domain.usecase.settings.GetStartDayOfMonthUseCase
import com.ivy.domain.usecase.settings.GetThemeUseCase
import com.ivy.domain.usecase.wallet.CalculateWalletBalanceUseCase
import com.ivy.domain.usecase.wallet.CalculateWalletIncomeExpenseUseCase
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.home.customerjourney.CustomerJourneyCardModel
import com.ivy.home.customerjourney.CustomerJourneyCardsProvider
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.period.PeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.data.model.toCloseTimeRange
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.preferences.asEnabledState
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.transaction.GetTransactionHistoryItemsUseCase
import com.ivy.data.model.ClosedTimeRange
import com.ivy.data.model.IncomeExpensePair
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
import javax.inject.Inject

@Stable
@HiltViewModel
internal class HomeViewModel @Inject internal constructor(
    private val themeState: ThemeState,
    private val customerJourneyCardsProvider: CustomerJourneyCardsProvider,
    private val getTransactionHistoryItemsUseCase: GetTransactionHistoryItemsUseCase,
    private val calculateWalletIncomeExpenseUseCase: CalculateWalletIncomeExpenseUseCase,
    private val calculateWalletBalanceUseCase: CalculateWalletBalanceUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getThemeUseCase: GetThemeUseCase,
    private val getStartDayOfMonth: GetStartDayOfMonthUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getHideCurrentBalancePreference: GetHideCurrentBalancePreferenceUseCase,
    private val getHideIncomePreference: GetHideIncomePreferenceUseCase,
    private val hasTransactionsUseCase: HasTransactionsUseCase,
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
            customerJourneyCards = getCustomerJourneyCards(),
            hideBalance = getHideBalance(),
            expanded = getExpanded(),
            hideIncome = getHideIncome(),
            shouldShowAccountSpecificColorInTransactions = getShouldShowAccountSpecificColorInTransactions(),
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
                is HomeEvent.SetPeriod -> setPeriod(event.period)
                HomeEvent.SelectNextMonth -> onSelectNextMonth()
                HomeEvent.SelectPreviousMonth -> onSelectPreviousMonth()
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

        val timeRange = periodState.rangeOf(period).toCloseTimeRange()

        val transactionListInput = loadTransactionListData(
            HomeRangeInput(
                preferences = preferences,
                timeRange = timeRange
            )
        )
        val balanceInput = loadIncomeExpenseBalance(transactionListInput)
        loadTransactionHistory(
            HomeHistoryInput(
                baseCurrency = balanceInput.preferences.baseCurrency,
                timeRange = balanceInput.timeRange
            )
        )
        loadCustomerJourney()
    }

    private suspend fun loadHomePreferences(): HomePreferences {
        return HomePreferences(
            theme = getThemeUseCase(),
            baseCurrency = getBaseCurrencyCode(),
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
                .map { it.toHomeTransactionListAccount() }
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

    private suspend fun loadTransactionHistory(
        input: HomeHistoryInput
    ) {
        history = getTransactionHistoryItemsUseCase(
            range = input.timeRange,
            baseCurrency = input.baseCurrency
        )
    }

    private suspend fun loadCustomerJourney() {
        customerJourneyCards = withContext(Dispatchers.IO) {
            customerJourneyCardsProvider.loadCards().toImmutableList()
        }
    }
// -----------------------------------------------------------------

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

private fun Account.toHomeTransactionListAccount() = HomeTransactionListAccount(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
    currency = asset.code,
)

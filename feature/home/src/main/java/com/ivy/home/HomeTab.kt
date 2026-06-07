package com.ivy.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivy.base.legacy.Transaction
import com.ivy.base.legacy.TransactionHistoryItem
import com.ivy.base.legacy.stringRes
import com.ivy.design.api.LocalTimeConverter
import com.ivy.design.api.LocalDatePicker
import com.ivy.design.api.LocalTimeFormatter
import com.ivy.design.api.LocalTimeProvider
import com.ivy.frp.forward
import com.ivy.frp.then2
import com.ivy.home.Constants.SWIPE_HORIZONTAL_THRESHOLD
import com.ivy.home.customerjourney.CustomerJourney
import com.ivy.home.customerjourney.CustomerJourneyCardModel
import com.ivy.legacy.ui.model.AppBaseData
import com.ivy.legacy.ui.model.BufferInfo
import com.ivy.legacy.ui.model.LegacyDueSection
import com.ivy.legacy.ui.model.period.Month
import com.ivy.legacy.ui.model.period.TimePeriod
import com.ivy.legacy.ui.state.LocalPeriodState
import com.ivy.legacy.ui.component.transaction.TransactionsDividerLine
import com.ivy.legacy.ui.component.transaction.transactions
import com.ivy.ui.legacy.horizontalSwipeListener
import com.ivy.ui.legacy.rememberSwipeListenerState
import com.ivy.ui.legacy.verticalSwipeListener
import com.ivy.navigation.screenScopedViewModel
import com.ivy.navigation.LocalMainTabState
import com.ivy.navigation.MainTab
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.legacy.domain.pure.data.IncomeExpensePair
import com.ivy.legacy.ui.modal.BufferModal
import com.ivy.legacy.ui.modal.BufferModalData
import com.ivy.legacy.ui.modal.ChoosePeriodModal
import com.ivy.legacy.ui.modal.ChoosePeriodModalData
import com.ivy.legacy.ui.modal.CurrencyModal
import com.ivy.legacy.ui.modal.DeleteModal
import kotlinx.collections.immutable.ImmutableList
import java.math.BigDecimal

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.HomeTab() {
    val viewModel: HomeViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    HomeUi(uiState, viewModel::onEvent)
}

@Suppress("LongMethod")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.HomeUi(
    uiState: HomeState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val periodState = LocalPeriodState.current
    val datePicker = LocalDatePicker.current
    val mainTabState = LocalMainTabState.current

    var bufferModalData: BufferModalData? by remember { mutableStateOf(null) }
    var currencyModalVisible by remember { mutableStateOf(false) }
    var choosePeriodModal: ChoosePeriodModalData? by remember {
        mutableStateOf(null)
    }
    var moreMenuExpanded by remember { mutableStateOf(false) }
    var skipAllModalVisible by remember { mutableStateOf(false) }
    val setMoreMenuExpanded = { expanded: Boolean ->
        moreMenuExpanded = expanded
    }

    val baseCurrency = uiState.baseData.baseCurrency

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalSwipeListener(
                sensitivity = Constants.SWIPE_DOWN_THRESHOLD_OPEN_MORE_MENU,
                state = rememberSwipeListenerState(),
                onSwipeDown = {
                    setMoreMenuExpanded(true)
                }
            )
            .horizontalSwipeListener(
                sensitivity = SWIPE_HORIZONTAL_THRESHOLD,
                state = rememberSwipeListenerState(),
                onSwipeLeft = {
                    mainTabState.select(MainTab.ACCOUNTS)
                },
                onSwipeRight = {
                    mainTabState.select(MainTab.ACCOUNTS)
                }
            )
    ) {
        val listState = rememberScrollPositionListState(
            key = "home_lazy_column"
        )

        HomeHeader(
            expanded = uiState.expanded,
            period = uiState.period,
            currency = baseCurrency,
            balance = uiState.balance.toDouble(),
            hideBalance = uiState.hideBalance,

            onShowMonthModal = {
                choosePeriodModal = ChoosePeriodModalData(
                    period = uiState.period
                )
            },
            onBalanceClick = {
                onEvent(HomeEvent.BalanceClick)
            },
            onHiddenBalanceClick = {
                onEvent(HomeEvent.HiddenBalanceClick)
            },
            onSelectNextMonth = {
                onEvent(HomeEvent.SelectNextMonth)
            },
            onSelectPreviousMonth = {
                onEvent(HomeEvent.SelectPreviousMonth)
            }
        )

        HomeLazyColumn(
            hideBalance = uiState.hideBalance,
            hideIncome = uiState.hideIncome,
            onSetExpand = {
                onEvent(HomeEvent.SetExpanded(it))
            },
            balance = uiState.balance,
            onOpenMoreMenu = {
                setMoreMenuExpanded(true)
            },
            onBalanceClick = {
                onEvent(HomeEvent.BalanceClick)
            },
            onHiddenBalanceClick = {
                onEvent(HomeEvent.HiddenBalanceClick)
            },
            onHiddenIncomeClick = {
                onEvent(HomeEvent.HiddenIncomeClick)
            },

            period = uiState.period,
            listState = listState,

            baseData = uiState.baseData,

            upcoming = uiState.upcoming,
            overdue = uiState.overdue,

            stats = uiState.stats,
            history = uiState.history,

            customerJourneyCards = uiState.customerJourneyCards,
            shouldShowAccountSpecificColorInTransactions = uiState.shouldShowAccountSpecificColorInTransactions,

            onPayOrGet = forward<Transaction>() then2 {
                HomeEvent.PayOrGetPlanned(it)
            } then2 onEvent,
            onDismiss = forward<CustomerJourneyCardModel>() then2 {
                HomeEvent.DismissCustomerJourneyCard(it)
            } then2 onEvent,
            onSkipTransaction = forward<Transaction>() then2 {
                HomeEvent.SkipPlanned(it)
            } then2 onEvent,
            setUpcomingExpanded = forward<Boolean>() then2 {
                HomeEvent.SetUpcomingExpanded(it)
            } then2 onEvent,
            setOverdueExpanded = forward<Boolean>() then2 {
                HomeEvent.SetOverdueExpanded(it)
            } then2 onEvent,
            onSkipAllTransactions = {
                skipAllModalVisible = true
            }
        )
    }

    MoreMenu(
        expanded = moreMenuExpanded,
        theme = uiState.theme,
        balance = uiState.balance.toDouble(),
        currency = baseCurrency,
        buffer = uiState.buffer.amount.toDouble(),
        onSwitchTheme = {
            onEvent(HomeEvent.SwitchTheme)
        },
        setExpanded = setMoreMenuExpanded,
        onBufferClick = {
            bufferModalData = BufferModalData(
                balance = uiState.balance.toDouble(),
                currency = baseCurrency,
                buffer = uiState.buffer.amount.toDouble()
            )
        },
        onCurrencyClick = {
            currencyModalVisible = true
        }
    )

    BufferModal(
        modal = bufferModalData,
        dismiss = {
            bufferModalData = null
        },
        onBufferChanged = forward<Double>() then2 {
            HomeEvent.SetBuffer(it)
        } then2 onEvent
    )

    CurrencyModal(
        title = stringResource(R.string.set_currency),
        initialCurrency = IvyCurrency.fromCode(baseCurrency),
        visible = currencyModalVisible,
        dismiss = {
            currencyModalVisible = false
        },
        onSetCurrency = forward<String>() then2 {
            HomeEvent.SetCurrency(it)
        } then2 onEvent
    )

    ChoosePeriodModal(
        modal = choosePeriodModal,
        dismiss = {
            choosePeriodModal = null
        },
        saveSelectedPeriod = periodState::select,
        pickDate = { minDate, maxDate, initialDate, onDatePicked ->
            datePicker.pickDate(
                minDate = minDate,
                maxDate = maxDate,
                initialDate = initialDate,
                onDatePicked = onDatePicked
            )
        },
        onPeriodSelected = forward<TimePeriod>() then2 {
            HomeEvent.SetPeriod(it)
        } then2 onEvent
    )

    DeleteModal(
        visible = skipAllModalVisible,
        title = stringResource(R.string.confirm_skip_all),
        description = stringResource(R.string.confirm_skip_all_description),
        dismiss = {
            skipAllModalVisible = false
        }
    ) {
        onEvent(HomeEvent.SkipAllPlanned(uiState.overdue.trns))
        skipAllModalVisible = false
    }
}

@Suppress("LongParameterList")
@ExperimentalAnimationApi
@Composable
fun HomeLazyColumn(
    hideBalance: Boolean,
    hideIncome: Boolean,
    onSetExpand: (Boolean) -> Unit,
    listState: LazyListState,
    period: TimePeriod,

    baseData: AppBaseData,
    shouldShowAccountSpecificColorInTransactions: Boolean,

    upcoming: LegacyDueSection,
    overdue: LegacyDueSection,
    balance: BigDecimal,
    stats: IncomeExpensePair,
    history: ImmutableList<TransactionHistoryItem>,

    customerJourneyCards: ImmutableList<CustomerJourneyCardModel>,

    setUpcomingExpanded: (Boolean) -> Unit,
    setOverdueExpanded: (Boolean) -> Unit,

    onOpenMoreMenu: () -> Unit,
    onBalanceClick: () -> Unit,

    onPayOrGet: (Transaction) -> Unit,
    onDismiss: (CustomerJourneyCardModel) -> Unit,
    onHiddenBalanceClick: () -> Unit,
    onHiddenIncomeClick: () -> Unit,
    onSkipTransaction: (Transaction) -> Unit,
    onSkipAllTransactions: (List<Transaction>) -> Unit,
    modifier: Modifier = Modifier
) {
    val periodState = LocalPeriodState.current

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                onSetExpand(listState.firstVisibleItemScrollOffset == 0)
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    val timeProvider = LocalTimeProvider.current
    val timeConverter = LocalTimeConverter.current
    val timeFormatter = LocalTimeFormatter.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .testTag("home_lazy_column"),
        state = listState
    ) {
        item {
            CashFlowInfo(
                currency = baseData.baseCurrency,
                balance = balance.toDouble(),

                hideBalance = hideBalance,

                monthlyIncome = stats.income.toDouble(),
                monthlyExpenses = stats.expense.toDouble(),

                onOpenMoreMenu = onOpenMoreMenu,
                onBalanceClick = onBalanceClick,
                onHiddenBalanceClick = onHiddenBalanceClick,
                percentExpanded = 1f,
                hideIncome = hideIncome,
                onHiddenIncomeClick = onHiddenIncomeClick
            )
        }
        item {
            Spacer(Modifier.height(16.dp))

            TransactionsDividerLine()
        }

        item {
            CustomerJourney(
                customerJourneyCards = customerJourneyCards,
                onDismiss = onDismiss
            )
        }

        transactions(
            baseData = baseData,
            upcoming = upcoming,
            setUpcomingExpanded = setUpcomingExpanded,
            overdue = overdue,
            setOverdueExpanded = setOverdueExpanded,
            history = history,
            onPayOrGet = onPayOrGet,
            emptyStateTitle = stringRes(R.string.no_transactions),
            emptyStateText = stringRes(
                R.string.no_transactions_description,
                period.toDisplayLong(
                    startDateOfMonth = periodState.startDayOfMonth,
                    timeProvider = timeProvider,
                    timeConverter = timeConverter,
                    timeFormatter = timeFormatter,
                )
            ),
            shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
            onSkipTransaction = onSkipTransaction,
            onSkipAllTransactions = onSkipAllTransactions
        )
    }
}

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
import androidx.compose.runtime.LaunchedEffect
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
import com.ivy.data.model.TransactionType
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.home.Constants.SWIPE_HORIZONTAL_THRESHOLD
import com.ivy.home.customerjourney.CustomerJourney
import com.ivy.home.customerjourney.CustomerJourneyAction
import com.ivy.home.customerjourney.CustomerJourneyCardModel
import com.ivy.legacy.ui.component.transaction.LegacyDueSection
import com.ivy.legacy.ui.component.transaction.TransactionListData
import com.ivy.ui.period.Month
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.period.displayLong
import com.ivy.ui.period.LocalPeriodState
import com.ivy.legacy.ui.component.transaction.TransactionsDividerLine
import com.ivy.legacy.ui.component.transaction.transactions
import com.ivy.ui.compose.horizontalSwipeListener
import com.ivy.ui.compose.rememberSwipeListenerState
import com.ivy.ui.compose.verticalSwipeListener
import com.ivy.ui.navigation.BalanceScreen
import com.ivy.ui.navigation.EditPlannedScreen
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.PieChartStatisticScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.data.model.IncomeExpensePair
import com.ivy.legacy.ui.modal.BufferModal
import com.ivy.legacy.ui.modal.BufferModalData
import com.ivy.legacy.ui.modal.ChoosePeriodModal
import com.ivy.legacy.ui.modal.ChoosePeriodModalData
import com.ivy.legacy.ui.modal.CurrencyModal
import com.ivy.legacy.ui.modal.DeleteModal
import kotlinx.collections.immutable.ImmutableList
import java.math.BigDecimal
import java.util.UUID

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.HomeTab(
    onOpenAccountsTab: () -> Unit,
) {
    val viewModel: HomeViewModel = screenScopedViewModel()
    val nav = navigation()
    val uiState = viewModel.uiState()

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                HomeUiEvent.OpenBalance -> nav.navigateTo(BalanceScreen)
                HomeUiEvent.OpenAccountsTab -> {
                    onOpenAccountsTab()
                    nav.navigateTo(MainScreen)
                }
            }
        }
    }

    HomeUi(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onOpenAccountsTab = onOpenAccountsTab,
    )
}

@Suppress("LongMethod")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
internal fun BoxWithConstraintsScope.HomeUi(
    uiState: HomeState,
    onEvent: (HomeEvent) -> Unit,
    onOpenAccountsTab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val periodState = LocalPeriodState.current
    val datePicker = LocalDatePicker.current

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
                    onOpenAccountsTab()
                },
                onSwipeRight = {
                    onOpenAccountsTab()
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

            onPayOrGet = { onEvent(HomeEvent.PayOrGetPlanned(it)) },
            onDismiss = { onEvent(HomeEvent.DismissCustomerJourneyCard(it)) },
            onSkipTransaction = { onEvent(HomeEvent.SkipPlanned(it)) },
            setUpcomingExpanded = { onEvent(HomeEvent.SetUpcomingExpanded(it)) },
            setOverdueExpanded = { onEvent(HomeEvent.SetOverdueExpanded(it)) },
            onOpenAccountsTab = onOpenAccountsTab,
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
        onBufferChanged = { onEvent(HomeEvent.SetBuffer(it)) }
    )

    CurrencyModal(
        title = stringResource(R.string.set_currency),
        initialCurrency = IvyCurrency.fromCode(baseCurrency),
        visible = currencyModalVisible,
        dismiss = {
            currencyModalVisible = false
        },
        onSetCurrency = { onEvent(HomeEvent.SetCurrency(it)) }
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
        onPeriodSelected = { onEvent(HomeEvent.SetPeriod(it)) }
    )

    DeleteModal(
        visible = skipAllModalVisible,
        title = stringResource(R.string.confirm_skip_all),
        description = stringResource(R.string.confirm_skip_all_description),
        dismiss = {
            skipAllModalVisible = false
        }
    ) {
        onEvent(HomeEvent.SkipAllPlanned(uiState.overdue.transactions.map { it.id }))
        skipAllModalVisible = false
    }
}

@Suppress("LongParameterList")
@ExperimentalAnimationApi
@Composable
internal fun HomeLazyColumn(
    hideBalance: Boolean,
    hideIncome: Boolean,
    onSetExpand: (Boolean) -> Unit,
    listState: LazyListState,
    period: TimePeriod,

    baseData: HomeTransactionListData,
    shouldShowAccountSpecificColorInTransactions: Boolean,

    upcoming: HomeDueSection,
    overdue: HomeDueSection,
    balance: BigDecimal,
    stats: IncomeExpensePair,
    history: ImmutableList<TransactionHistoryItem>,

    customerJourneyCards: ImmutableList<CustomerJourneyCardModel>,

    setUpcomingExpanded: (Boolean) -> Unit,
    setOverdueExpanded: (Boolean) -> Unit,

    onOpenMoreMenu: () -> Unit,
    onBalanceClick: () -> Unit,

    onPayOrGet: (UUID) -> Unit,
    onDismiss: (CustomerJourneyCardModel) -> Unit,
    onHiddenBalanceClick: () -> Unit,
    onHiddenIncomeClick: () -> Unit,
    onSkipTransaction: (UUID) -> Unit,
    onSkipAllTransactions: (List<UUID>) -> Unit,
    onOpenAccountsTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val periodState = LocalPeriodState.current
    val nav = navigation()

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

    val noTransactionsTitle = stringResource(R.string.no_transactions)
    val noTransactionsText = stringResource(
        R.string.no_transactions_description,
        period.displayLong(periodState.startDayOfMonth)
    )
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
                onDismiss = onDismiss,
                onAction = { action ->
                    when (action) {
                        CustomerJourneyAction.OpenAccountsTab -> {
                            onOpenAccountsTab()
                        }

                        CustomerJourneyAction.AddPlannedPayment -> {
                            nav.navigateTo(
                                EditPlannedScreen(
                                    type = TransactionRouteType.EXPENSE,
                                    plannedPaymentRuleId = null
                                )
                            )
                        }

                        CustomerJourneyAction.OpenExpensePieChart -> {
                            nav.navigateTo(
                                PieChartStatisticScreen(type = TransactionRouteType.EXPENSE)
                            )
                        }
                    }
                },
            )
        }

        transactions(
            baseData = baseData.toTransactionListData(),
            upcoming = upcoming.toLegacyDueSection(),
            setUpcomingExpanded = setUpcomingExpanded,
            overdue = overdue.toLegacyDueSection(),
            setOverdueExpanded = setOverdueExpanded,
            history = history,
            onPayOrGet = onPayOrGet,
            onTransactionClick = { transactionId, transactionType ->
                nav.navigateTo(
                    EditTransactionScreen(
                        initialTransactionId = transactionId,
                        type = transactionType.toRouteType()
                    )
                )
            },
            onAccountClick = {
                nav.navigateTo(
                    TransactionsScreen(
                        accountId = it,
                        categoryId = null
                    )
                )
            },
            onCategoryClick = {
                nav.navigateTo(
                    TransactionsScreen(
                        accountId = null,
                        categoryId = it
                    )
                )
            },
            emptyStateTitle = noTransactionsTitle,
            emptyStateText = noTransactionsText,
            shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
            onSkipTransaction = onSkipTransaction,
            onSkipAllTransactions = onSkipAllTransactions
        )
    }
}

private fun HomeTransactionListData.toTransactionListData(): TransactionListData {
    return TransactionListData(
        baseCurrency = baseCurrency,
        accounts = accounts,
        categories = categories
    )
}

private fun HomeDueSection.toLegacyDueSection(): LegacyDueSection {
    return LegacyDueSection(
        transactions = transactions,
        expanded = expanded,
        stats = stats
    )
}

private fun TransactionType.toRouteType(): TransactionRouteType {
    return TransactionRouteType.valueOf(name)
}

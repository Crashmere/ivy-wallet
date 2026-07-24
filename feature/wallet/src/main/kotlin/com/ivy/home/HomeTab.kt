package com.ivy.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.Category
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Tag
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionHistoryDateDivider
import com.ivy.data.model.TransactionType
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.TransactionHistoryTransaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getFromValue
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.home.Constants.SWIPE_HORIZONTAL_THRESHOLD
import com.ivy.home.customerjourney.CustomerJourney
import com.ivy.home.customerjourney.CustomerJourneyAction
import com.ivy.home.customerjourney.CustomerJourneyCardModel
import com.ivy.ui.transaction.TransactionListAccount
import com.ivy.ui.transaction.TransactionListCategory
import com.ivy.ui.transaction.TransactionListData
import com.ivy.ui.transaction.TransactionListHistoryDateDivider
import com.ivy.ui.transaction.TransactionListHistoryItem
import com.ivy.ui.transaction.TransactionListHistoryTransaction
import com.ivy.ui.transaction.TransactionListTag
import com.ivy.ui.transaction.TransactionListTransaction
import com.ivy.ui.transaction.TransactionListTransactionType
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.period.displayLong
import com.ivy.ui.period.LocalPeriodState
import com.ivy.ui.transaction.transactions
import com.ivy.ui.compose.horizontalSwipeListener
import com.ivy.ui.compose.rememberSwipeListenerState
import com.ivy.ui.navigation.BalanceScreen
import com.ivy.ui.navigation.BulkEditScreen
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.PieChartStatisticScreen
import com.ivy.ui.navigation.SearchScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.data.model.IncomeExpensePair
import com.ivy.ui.modal.ChoosePeriodModal
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
        onOpenSearch = {
            nav.navigateTo(SearchScreen)
        },
        onOpenBulkEdit = {
            nav.navigateTo(BulkEditScreen)
        },
        onOpenIncomePieChart = {
            nav.navigateTo(PieChartStatisticScreen(type = TransactionRouteType.INCOME))
        },
        onOpenExpensePieChart = {
            nav.navigateTo(PieChartStatisticScreen(type = TransactionRouteType.EXPENSE))
        },
        onTransactionClick = { transactionId, transactionType ->
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = transactionId,
                    type = transactionType.toRouteType()
                )
            )
        },
        onAccountClick = { accountId ->
            nav.navigateTo(
                TransactionsScreen(
                    accountId = accountId,
                    categoryId = null
                )
            )
        },
        onCategoryClick = { categoryId ->
            nav.navigateTo(
                TransactionsScreen(
                    accountId = null,
                    categoryId = categoryId
                )
            )
        },
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
    onOpenSearch: () -> Unit,
    onOpenBulkEdit: () -> Unit,
    onOpenIncomePieChart: () -> Unit,
    onOpenExpensePieChart: () -> Unit,
    onTransactionClick: (UUID, TransactionType) -> Unit,
    onAccountClick: (UUID) -> Unit,
    onCategoryClick: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    val periodState = LocalPeriodState.current
    val datePicker = LocalDatePicker.current

    var choosePeriodModal: TimePeriod? by remember {
        mutableStateOf(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
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
            onShowMonthModal = {
                choosePeriodModal = uiState.period
            },
            onSelectNextMonth = {
                onEvent(HomeEvent.SelectNextMonth)
            },
            onSelectPreviousMonth = {
                onEvent(HomeEvent.SelectPreviousMonth)
            },
            onOpenSearch = onOpenSearch,
            onOpenBulkEdit = onOpenBulkEdit,
        )

        HomeLazyColumn(
            hideBalance = uiState.hideBalance,
            hideIncome = uiState.hideIncome,
            onSetExpand = {
                onEvent(HomeEvent.SetExpanded(it))
            },
            balance = uiState.balance,
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

            stats = uiState.stats,
            history = uiState.history,

            customerJourneyCards = uiState.customerJourneyCards,
            shouldShowAccountSpecificColorInTransactions = uiState.shouldShowAccountSpecificColorInTransactions,

            onDismiss = { onEvent(HomeEvent.DismissCustomerJourneyCard(it)) },
            onOpenAccountsTab = onOpenAccountsTab,
            onOpenIncomePieChart = onOpenIncomePieChart,
            onOpenExpensePieChart = onOpenExpensePieChart,
            onTransactionClick = onTransactionClick,
            onAccountClick = onAccountClick,
            onCategoryClick = onCategoryClick,
        )
    }

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

    balance: BigDecimal,
    stats: IncomeExpensePair,
    history: ImmutableList<TransactionHistoryItem>,

    customerJourneyCards: ImmutableList<CustomerJourneyCardModel>,

    onBalanceClick: () -> Unit,

    onDismiss: (CustomerJourneyCardModel) -> Unit,
    onHiddenBalanceClick: () -> Unit,
    onHiddenIncomeClick: () -> Unit,
    onOpenAccountsTab: () -> Unit,
    onOpenIncomePieChart: () -> Unit,
    onOpenExpensePieChart: () -> Unit,
    onTransactionClick: (UUID, TransactionType) -> Unit,
    onAccountClick: (UUID) -> Unit,
    onCategoryClick: (UUID) -> Unit,
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

    val noTransactionsTitle = stringResource(R.string.no_transactions)
    val noTransactionsText = stringResource(
        R.string.no_transactions_description,
        period.displayLong(periodState.startDayOfMonth)
    )
    val balanceTrend = remember(history) {
        history
            .filterIsInstance<TransactionHistoryDateDivider>()
            .sortedBy { it.date }
            .runningFold(0f) { acc, divider ->
                acc + (divider.income - divider.expenses).toFloat()
            }
            .drop(1)
            .toImmutableList()
    }
    var typeFilter by remember { mutableStateOf(HomeTypeFilter.ALL) }
    val filteredHistory = remember(history, typeFilter) {
        if (typeFilter == HomeTypeFilter.ALL) {
            history
        } else {
            val mapped = history.mapNotNull { item ->
                when (item) {
                    is TransactionHistoryTransaction ->
                        item.takeIf { matchesType(it.transaction, typeFilter) }

                    is TransactionHistoryDateDivider -> when (typeFilter) {
                        HomeTypeFilter.EXPENSE -> item.copy(income = 0.0)
                        HomeTypeFilter.INCOME -> item.copy(expenses = 0.0)
                        HomeTypeFilter.ALL -> item
                    }

                    else -> item
                }
            }
            mapped.filterIndexed { index, item ->
                if (item is TransactionHistoryDateDivider) {
                    mapped.getOrNull(index + 1) is TransactionHistoryTransaction
                } else {
                    true
                }
            }.toImmutableList()
        }
    }
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

                onBalanceClick = onBalanceClick,
                onHiddenBalanceClick = onHiddenBalanceClick,
                hideIncome = hideIncome,
                onHiddenIncomeClick = onHiddenIncomeClick,
                onOpenIncomePieChart = onOpenIncomePieChart,
                onOpenExpensePieChart = onOpenExpensePieChart,
                trend = balanceTrend
            )
        }
        item {
            Spacer(Modifier.height(16.dp))

            HomeTypeFilterRow(
                selected = typeFilter,
                onSelect = { typeFilter = it },
            )

            Spacer(Modifier.height(4.dp))

            HomeTransactionsDividerLine()
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

                        CustomerJourneyAction.OpenExpensePieChart -> {
                            onOpenExpensePieChart()
                        }
                    }
                },
            )
        }

        transactions(
            baseData = baseData.toTransactionListData(),
            upcoming = null,
            setUpcomingExpanded = {},
            overdue = null,
            setOverdueExpanded = {},
            history = filteredHistory.map { it.toTransactionListHistoryItem() },
            onPayOrGet = {},
            onTransactionClick = { transactionId, transactionType ->
                onTransactionClick(transactionId, transactionType.toTransactionType())
            },
            onAccountClick = onAccountClick,
            onCategoryClick = onCategoryClick,
            emptyStateTitle = noTransactionsTitle,
            emptyStateText = noTransactionsText,
            shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
        )
    }
}

private enum class HomeTypeFilter { ALL, EXPENSE, INCOME }

private fun matchesType(transaction: Transaction, filter: HomeTypeFilter): Boolean = when (filter) {
    HomeTypeFilter.ALL -> true
    HomeTypeFilter.EXPENSE -> transaction is Expense
    HomeTypeFilter.INCOME -> transaction is Income
}

@Composable
private fun HomeTypeFilterRow(
    selected: HomeTypeFilter,
    onSelect: (HomeTypeFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HomeFilterChip(
            text = stringResource(R.string.all),
            selected = selected == HomeTypeFilter.ALL,
            onClick = { onSelect(HomeTypeFilter.ALL) },
        )

        Spacer(Modifier.width(10.dp))

        HomeFilterChip(
            text = stringResource(R.string.expense),
            selected = selected == HomeTypeFilter.EXPENSE,
            onClick = { onSelect(HomeTypeFilter.EXPENSE) },
        )

        Spacer(Modifier.width(10.dp))

        HomeFilterChip(
            text = stringResource(R.string.income),
            selected = selected == HomeTypeFilter.INCOME,
            onClick = { onSelect(HomeTypeFilter.INCOME) },
        )
    }
}

@Composable
private fun HomeFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(percent = 50)
    Text(
        modifier = Modifier
            .clip(shape)
            .then(
                if (selected) {
                    Modifier.background(HomeTheme.colors.pureInverse, shape)
                } else {
                    Modifier.border(1.dp, HomeTheme.colors.medium, shape)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        text = text,
        style = HomeTheme.typo.b2.copy(
            fontWeight = FontWeight.Bold,
            color = if (selected) HomeTheme.colors.pure else HomeTheme.colors.pureInverse,
            textAlign = TextAlign.Start,
        ),
    )
}

private fun HomeTransactionListData.toTransactionListData(): TransactionListData {
    return TransactionListData(
        baseCurrency = baseCurrency,
        accounts = accounts
            .map { it.toTransactionListAccount() },
        categories = categories.map { it.toTransactionListCategory() }
    )
}

private fun HomeTransactionListAccount.toTransactionListAccount() = TransactionListAccount(
    id = id,
    name = name,
    color = color,
    icon = icon,
    currency = currency,
)

private fun Category.toTransactionListCategory() = TransactionListCategory(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
)

private fun TransactionHistoryItem.toTransactionListHistoryItem(): TransactionListHistoryItem {
    return when (this) {
        is TransactionHistoryTransaction -> TransactionListHistoryTransaction(
            transaction = transaction.toTransactionListTransaction(),
            tags = tags.map { it.toTransactionListTag() },
        )

        is TransactionHistoryDateDivider -> TransactionListHistoryDateDivider(
            date = date,
            income = income,
            expenses = expenses,
        )

        else -> error("Unsupported transaction history item: ${this::class.simpleName}")
    }
}

private fun Tag.toTransactionListTag() = TransactionListTag(
    id = id.value,
    name = name.value,
)

private fun TransactionListTransactionType.toTransactionType(): TransactionType {
    return TransactionType.valueOf(name)
}

private fun Transaction.toTransactionListTransaction(): TransactionListTransaction {
    val amount = getFromValue().amount.value.toBigDecimal()
    return TransactionListTransaction(
        id = id.value,
        accountId = getFromAccount().value,
        type = when (this) {
            is Expense -> TransactionListTransactionType.EXPENSE
            is Income -> TransactionListTransactionType.INCOME
            is Transfer -> TransactionListTransactionType.TRANSFER
        },
        amount = amount,
        toAccountId = if (this is Transfer) toAccount.value else null,
        toAmount = if (this is Transfer) toValue.amount.value.toBigDecimal() else amount,
        title = title?.value,
        description = description?.value,
        dateTime = time.takeIf { settled },
        categoryId = category?.value,
        dueDate = time.takeIf { !settled },
        recurringRuleId = metadata.recurringRuleId,
        paidFor = metadata.paidForDateTime,
    )
}

private fun TransactionType.toRouteType(): TransactionRouteType {
    return TransactionRouteType.valueOf(name)
}

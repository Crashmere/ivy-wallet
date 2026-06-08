package com.ivy.reports

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.data.model.TransactionType
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.transaction.DueSection
import com.ivy.legacy.ui.transaction.TransactionListAccount
import com.ivy.legacy.ui.transaction.TransactionListData
import com.ivy.legacy.ui.summary.IncomeExpensesCards
import com.ivy.legacy.ui.transaction.transactions
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.PieChartStatisticScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.R
import com.ivy.ui.platform.fileSharer
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.data.model.IncomeExpensePair
import com.ivy.legacy.ui.money.BalanceRow
import com.ivy.legacy.ui.button.IvyButton
import com.ivy.legacy.ui.icon.IvyIcon
import com.ivy.legacy.ui.button.IvyOutlinedButton
import com.ivy.ui.compose.FilledIconButton
import kotlinx.collections.immutable.toImmutableList

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.ReportScreen() {
    val viewModel: ReportViewModel = screenScopedViewModel()
    val state = viewModel.uiState()
    val platformFileSharer = fileSharer()

    LaunchedEffect(viewModel, platformFileSharer) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is ReportUiEvent.ShareCsvFile -> platformFileSharer.shareCSVFile(event.fileUri)
            }
        }
    }

    UI(
        state = state,
        onEventHandler = viewModel::onEvent
    )
}

@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    state: ReportScreenState = ReportScreenState(),
    onEventHandler: (ReportScreenEvent) -> Unit = {}
) {
    val transactionSummary = state.transactionSummary
    val nav = navigation()

    val listState = rememberScrollPositionListState(key = "reports")
    val noTransactionsTitle = stringResource(R.string.no_transactions)
    val noTransactionsForFilter = stringResource(R.string.no_transactions_for_your_filter)

    if (state.loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1000f)
                .background(LegacyTheme.colors.pure.copy(alpha = 0.95f))
                .clickableNoIndication(rememberInteractionSource()) {
                    // consume clicks
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.generating_report),
                style = LegacyTheme.typo.b1.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = LegacyTheme.colors.orange,
                    textAlign = TextAlign.Start
                )
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        state = listState
    ) {
        stickyHeader {
            Toolbar(
                onBack = { nav.back() },
                onExport = {
                    onEventHandler.invoke(ReportScreenEvent.OnExport)
                },
                onFilter = {
                    onEventHandler.invoke(
                        ReportScreenEvent.OnFilterOverlayVisible(
                            filterOverlayVisible = true
                        )
                    )
                }
            )
        }

        item {
            Text(
                modifier = Modifier.padding(
                    start = 32.dp
                ),
                text = stringResource(R.string.reports),
                style = LegacyTheme.typo.h2.copy(
                    color = LegacyTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                )
            )

            Spacer(Modifier.height(8.dp))

            BalanceRow(
                modifier = Modifier
                    .padding(start = 32.dp),
                textColor = LegacyTheme.colors.pureInverse,
                currency = state.baseCurrency,
                balance = state.balance,
                balanceAmountPrefix = when {
                    state.balance > 0 -> "+"
                    else -> null
                }
            )

            Spacer(Modifier.height(20.dp))

            IncomeExpensesCards(
                currency = state.baseCurrency,
                income = state.income,
                expenses = state.expenses,
                incomeTransactionCount = transactionSummary.incomeTransactionCount,
                expenseTransactionCount = transactionSummary.expenseTransactionCount,
                hasAddButtons = false,
                itemColor = LegacyTheme.colors.pure,
                incomeHeaderCardClicked = {
                    if (transactionSummary.hasTransactions) {
                        nav.navigateTo(
                            PieChartStatisticScreen(
                                type = TransactionRouteType.INCOME,
                                transactionIds = transactionSummary.transactionIds,
                                accountIdFilterList = state.accountIdFilters,
                                treatTransfersAsIncomeExpense = state.treatTransfersAsIncExp
                            )
                        )
                    }
                },
                expenseHeaderCardClicked = {
                    if (transactionSummary.hasTransactions) {
                        nav.navigateTo(
                            PieChartStatisticScreen(
                                type = TransactionRouteType.EXPENSE,
                                transactionIds = transactionSummary.transactionIds,
                                accountIdFilterList = state.accountIdFilters,
                                treatTransfersAsIncomeExpense = state.treatTransfersAsIncExp
                            )
                        )
                    }
                }
            )

            if (state.showTransfersAsIncExpCheckbox) {
                ReportCheckboxWithText(
                    modifier = Modifier
                        .padding(16.dp),
                    text = stringResource(R.string.transfers_as_income_expense),
                    checked = state.treatTransfersAsIncExp
                ) {
                    onEventHandler.invoke(
                        ReportScreenEvent.OnTreatTransfersAsIncomeExpense(
                            transfersAsIncomeExpense = it
                        )
                    )
                }
            } else {
                Spacer(Modifier.height(32.dp))
            }

            ReportTransactionsDividerLine(
                paddingHorizontal = 0.dp
            )

            Spacer(Modifier.height(4.dp))
        }

        if (state.filter != null) {
            transactions(
                baseData = TransactionListData(
                    baseCurrency = state.baseCurrency,
                    categories = state.categories,
                    accounts = state.accounts.map { it.toTransactionListAccount() }.toImmutableList(),
                ),

                upcoming = state.upcoming.toDueSection(),

                setUpcomingExpanded = {
                    onEventHandler.invoke(ReportScreenEvent.OnUpcomingExpanded(upcomingExpanded = it))
                },

                overdue = state.overdue.toDueSection(),
                setOverdueExpanded = {
                    onEventHandler.invoke(ReportScreenEvent.OnOverdueExpanded(overdueExpanded = it))
                },

                history = state.history,
                lastItemSpacer = 48.dp,

                onPayOrGet = {
                    onEventHandler.invoke(ReportScreenEvent.OnPayOrGetTransaction(transactionId = it))
                },
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
                emptyStateText = noTransactionsForFilter,
                shouldShowAccountSpecificColorInTransactions = state.showAccountColorsInTransactions,
                onSkipTransaction = {
                    onEventHandler.invoke(ReportScreenEvent.SkipTransaction(transactionId = it))
                },
                onSkipAllTransactions = {
                    onEventHandler.invoke(
                        ReportScreenEvent.SkipTransactions(
                            transactionIds = it
                        )
                    )
                }
            )
        } else {
            item {
                NoFilterEmptyState(
                    setFilterOverlayVisible = {
                        onEventHandler.invoke(
                            ReportScreenEvent.OnFilterOverlayVisible(
                                filterOverlayVisible = it
                            )
                        )
                    }
                )
            }
        }
    }

    FilterOverlay(
        visible = state.filterOverlayVisible,
        baseCurrency = state.baseCurrency,
        accounts = state.accounts,
        categories = state.categories,
        filter = state.filter,
        allTags = state.allTags,
        onClose = {
            onEventHandler.invoke(
                ReportScreenEvent.OnFilterOverlayVisible(
                    filterOverlayVisible = false
                )
            )
        },
        onSetFilter = {
            onEventHandler.invoke(ReportScreenEvent.OnFilter(filter = it))
        },
        onTagSearch = {
            onEventHandler.invoke(ReportScreenEvent.OnTagSearch(data = it))
        }
    )
}

@Composable
private fun ReportTransactionsDividerLine(
    paddingHorizontal: Dp = 24.dp,
) {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddingHorizontal),
        color = LegacyTheme.colors.medium,
        thickness = 2.dp
    )
}

private fun ReportDueSection.toDueSection(): DueSection {
    return DueSection(
        transactions = transactions,
        stats = IncomeExpensePair(
            income = income.toBigDecimal(),
            expense = expenses.toBigDecimal()
        ),
        expanded = expanded
    )
}

private fun ReportAccount.toTransactionListAccount() = TransactionListAccount(
    id = id,
    name = name,
    color = color,
    icon = icon,
    currency = currency,
)

@Composable
private fun NoFilterEmptyState(
    setFilterOverlayVisible: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        IvyIcon(
            icon = R.drawable.ic_filter_l,
            tint = LegacyTheme.colors.gray
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.no_filter),
            style = LegacyTheme.typo.b1.copy(
                color = LegacyTheme.colors.gray,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.invalid_filter_warning),
            style = LegacyTheme.typo.b2.copy(
                color = LegacyTheme.colors.gray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )

        Spacer(Modifier.height(32.dp))

        IvyButton(
            iconStart = R.drawable.ic_filter_xs,
            text = stringResource(R.string.set_filter)
        ) {
            setFilterOverlayVisible(true)
        }

        Spacer(Modifier.height(96.dp))
    }
}

private fun TransactionType.toRouteType(): TransactionRouteType {
    return TransactionRouteType.valueOf(name)
}

@Composable
private fun Toolbar(
    onBack: () -> Unit,
    onExport: () -> Unit,
    onFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LegacyTheme.colors.pure)
            .padding(top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(20.dp))

        Icon(
            modifier = Modifier
                .testTag("toolbar_close")
                .clip(CircleShape)
                .background(LegacyTheme.colors.pure, CircleShape)
                .border(2.dp, LegacyTheme.colors.medium, CircleShape)
                .clickable(onClick = onBack)
                .padding(6.dp),
            painter = painterResource(id = R.drawable.ic_dismiss),
            contentDescription = "close",
            tint = LegacyTheme.colors.pureInverse,
        )

        Spacer(Modifier.weight(1f))

        // Export CSV
        IvyOutlinedButton(
            text = stringResource(R.string.export),
            iconTint = LegacyTheme.colors.green,
            textColor = LegacyTheme.colors.green,
            solidBackground = true,
            padding = 8.dp,
            iconStart = R.drawable.ic_export_csv
        ) {
            onExport()
        }

        Spacer(Modifier.width(16.dp))

        // Filter
        FilledIconButton(
            icon = R.drawable.ic_filter_xs,
            backgroundColor = LegacyTheme.colors.medium,
            tint = LegacyTheme.colors.pureInverse,
        ) {
            onFilter()
        }

        Spacer(Modifier.width(24.dp))
    }
}

package com.ivy.reports

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.data.model.TransactionType
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.theme.system.style
import com.ivy.legacy.ui.component.transaction.LegacyDueSection
import com.ivy.legacy.ui.component.transaction.TransactionListData
import com.ivy.legacy.ui.component.IncomeExpensesCards
import com.ivy.legacy.ui.component.transaction.TransactionsDividerLine
import com.ivy.legacy.ui.component.transaction.transactions
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.PieChartStatisticScreen
import com.ivy.ui.navigation.ReportScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.R
import com.ivy.ui.platform.fileSharer
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.data.model.IncomeExpensePair
import com.ivy.legacy.ui.theme.Gray
import com.ivy.legacy.ui.theme.Green
import com.ivy.legacy.ui.theme.Orange
import com.ivy.legacy.ui.component.BackButtonType
import com.ivy.legacy.ui.component.BalanceRow
import com.ivy.legacy.ui.component.CircleButtonFilled
import com.ivy.legacy.ui.component.IvyButton
import com.ivy.legacy.ui.component.IvyCheckboxWithText
import com.ivy.legacy.ui.component.IvyIcon
import com.ivy.legacy.ui.component.IvyOutlinedButton
import com.ivy.legacy.ui.component.IvyToolbar
import com.ivy.legacy.ui.theme.pureBlur
import kotlinx.collections.immutable.toImmutableList

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.ReportScreen(
    screen: ReportScreen
) {
    val viewModel: ReportViewModel = screenScopedViewModel()
    val state = viewModel.uiState()

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
    val legacyTransactions = state.transactions
    val nav = navigation()
    val platformFileSharer = fileSharer()

    val listState = rememberScrollPositionListState(key = "reports")
    val noTransactionsTitle = stringResource(R.string.no_transactions)
    val noTransactionsForFilter = stringResource(R.string.no_transactions_for_your_filter)

    if (state.loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1000f)
                .background(pureBlur())
                .clickableNoIndication(rememberInteractionSource()) {
                    // consume clicks
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.generating_report),
                style = LegacyTheme.typo.b1.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = Orange
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
                onExport = {
                    onEventHandler.invoke(
                        ReportScreenEvent.OnExport(fileSharer = platformFileSharer)
                    )
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
                style = LegacyTheme.typo.h2.style(
                    fontWeight = FontWeight.ExtraBold
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
                history = state.history,
                currency = state.baseCurrency,
                income = state.income,
                expenses = state.expenses,
                hasAddButtons = false,
                itemColor = LegacyTheme.colors.pure,
                incomeHeaderCardClicked = {
                    if (state.transactions.isNotEmpty()) {
                        nav.navigateTo(
                            PieChartStatisticScreen(
                                type = TransactionRouteType.INCOME,
                                legacyTransactionIds = legacyTransactions
                                    .map { it.id }
                                    .toImmutableList(),
                                accountList = state.accountIdFilters,
                                treatTransfersAsIncomeExpense = state.treatTransfersAsIncExp
                            )
                        )
                    }
                },
                expenseHeaderCardClicked = {
                    if (state.transactions.isNotEmpty()) {
                        nav.navigateTo(
                            PieChartStatisticScreen(
                                type = TransactionRouteType.EXPENSE,
                                legacyTransactionIds = legacyTransactions
                                    .map { it.id }
                                    .toImmutableList(),
                                accountList = state.accountIdFilters,
                                treatTransfersAsIncomeExpense = state.treatTransfersAsIncExp
                            )
                        )
                    }
                }
            )

            if (state.showTransfersAsIncExpCheckbox) {
                IvyCheckboxWithText(
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

            TransactionsDividerLine(
                paddingHorizontal = 0.dp
            )

            Spacer(Modifier.height(4.dp))
        }

        if (state.filter != null) {
            transactions(
                baseData = TransactionListData(
                    baseCurrency = state.baseCurrency,
                    categories = state.categories,
                    accounts = state.accounts,
                ),

                upcoming = LegacyDueSection(
                    transactions = state.upcomingTransactions,
                    stats = IncomeExpensePair(
                        income = state.upcomingIncome.toBigDecimal(),
                        expense = state.upcomingExpenses.toBigDecimal()
                    ),
                    expanded = state.upcomingExpanded
                ),

                setUpcomingExpanded = {
                    onEventHandler.invoke(ReportScreenEvent.OnUpcomingExpanded(upcomingExpanded = it))
                },

                overdue = LegacyDueSection(
                    transactions = state.overdueTransactions,
                    stats = IncomeExpensePair(
                        income = state.overdueIncome.toBigDecimal(),
                        expense = state.overdueExpenses.toBigDecimal()
                    ),
                    expanded = state.overdueExpanded
                ),
                setOverdueExpanded = {
                    onEventHandler.invoke(ReportScreenEvent.OnOverdueExpanded(overdueExpanded = it))
                },

                history = state.history,
                lastItemSpacer = 48.dp,

                onPayOrGet = {
                    onEventHandler.invoke(ReportScreenEvent.OnPayOrGetLegacyTransaction(transaction = it))
                },
                onTransactionClick = {
                    nav.navigateTo(
                        EditTransactionScreen(
                            initialTransactionId = it.id,
                            type = it.type.toRouteType()
                        )
                    )
                },
                onAccountClick = {
                    nav.navigateTo(
                        TransactionsScreen(
                            accountId = it.id,
                            categoryId = null
                        )
                    )
                },
                onCategoryClick = {
                    nav.navigateTo(
                        TransactionsScreen(
                            accountId = null,
                            categoryId = it.id.value
                        )
                    )
                },
                emptyStateTitle = noTransactionsTitle,
                emptyStateText = noTransactionsForFilter,
                shouldShowAccountSpecificColorInTransactions = state.showAccountColorsInTransactions,
                onSkipTransaction = {
                    onEventHandler.invoke(ReportScreenEvent.SkipLegacyTransaction(transaction = it))
                },
                onSkipAllTransactions = {
                    onEventHandler.invoke(ReportScreenEvent.SkipLegacyTransactions(transactions = it))
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
            tint = Gray
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.no_filter),
            style = LegacyTheme.typo.b1.style(
                color = Gray,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.invalid_filter_warning),
            style = LegacyTheme.typo.b2.style(
                color = Gray,
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
    onExport: () -> Unit,
    onFilter: () -> Unit
) {
    val nav = navigation()
    IvyToolbar(
        backButtonType = BackButtonType.CLOSE,
        onBack = {
            nav.back()
        }
    ) {
        Spacer(Modifier.weight(1f))

        // Export CSV
        IvyOutlinedButton(
            text = stringResource(R.string.export),
            iconTint = Green,
            textColor = Green,
            solidBackground = true,
            padding = 8.dp,
            iconStart = R.drawable.ic_export_csv
        ) {
            onExport()
        }

        Spacer(Modifier.width(16.dp))

        // Filter
        CircleButtonFilled(
            icon = R.drawable.ic_filter_xs
        ) {
            onFilter()
        }

        Spacer(Modifier.width(24.dp))
    }
}

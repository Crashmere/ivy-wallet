package com.ivy.legacy.ui.component.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.TransactionHistoryDateDivider
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.theme.system.style
import com.ivy.legacy.ui.model.AppBaseData
import com.ivy.legacy.ui.model.LegacyDueSection
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.Navigation
import com.ivy.ui.navigation.navigation
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Black
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.Gray
import com.ivy.legacy.ui.theme.Orange
import com.ivy.legacy.ui.theme.Red
import com.ivy.legacy.ui.theme.White
import com.ivy.legacy.ui.component.IvyButton
import com.ivy.legacy.ui.component.IvyIcon

fun LazyListScope.transactions(
    baseData: AppBaseData,

    upcoming: LegacyDueSection?,
    overdue: LegacyDueSection?,
    history: List<TransactionHistoryItem>,

    emptyStateTitle: String,
    emptyStateText: String,

    dateDividerMarginTop: Dp? = null,
    lastItemSpacer: Dp? = null,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (LegacyTransaction) -> Unit,
    setUpcomingExpanded: (Boolean) -> Unit,
    setOverdueExpanded: (Boolean) -> Unit,
    onSkipTransaction: (LegacyTransaction) -> Unit = {},
    onSkipAllTransactions: (List<LegacyTransaction>) -> Unit = {}
) {
    upcomingSection(
        baseData = baseData,
        upcoming = upcoming,
        shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
        onPayOrGet = onPayOrGet,
        onSkipTransaction = onSkipTransaction,
        setExpanded = setUpcomingExpanded
    )

    overdueSection(
        baseData = baseData,
        overdue = overdue,

        onPayOrGet = onPayOrGet,
        onSkipTransaction = onSkipTransaction,
        onSkipAllTransactions = onSkipAllTransactions,
        shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
        setExpanded = setOverdueExpanded
    )

    historySection(
        baseData = baseData,

        history = history,
        shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
        dateDividerMarginTop = dateDividerMarginTop,
        onPayOrGet = onPayOrGet
    )

    if (
        (upcoming == null || upcoming.transactions.isEmpty()) &&
        (overdue == null || overdue.transactions.isEmpty()) &&
        history.isEmpty()
    ) {
        item {
            NoTransactionsEmptyState(
                emptyStateTitle = emptyStateTitle,
                emptyStateText = emptyStateText
            )
        }
    }

    scrollHackSpacer(
        history = history,
        upcoming = upcoming,
        overdue = overdue,
        lastItemSpacer = lastItemSpacer
    )
}

private fun LazyListScope.upcomingSection(
    baseData: AppBaseData,

    upcoming: LegacyDueSection?,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (LegacyTransaction) -> Unit,
    onSkipTransaction: (LegacyTransaction) -> Unit,
    setExpanded: (Boolean) -> Unit
) {
    if (upcoming == null) return // guard

    if (upcoming.transactions.isNotEmpty()) {
        item(
            key = "upcoming_section"
        ) {
            SectionDivider(
                expanded = upcoming.expanded,
                setExpanded = setExpanded,
                title = stringResource(R.string.upcoming),
                titleColor = Orange,
                baseCurrency = baseData.baseCurrency,
                income = upcoming.stats.income.toDouble(),
                expenses = upcoming.stats.expense.abs().toDouble()
            )
        }

        if (upcoming.expanded) {
            transactionItems(
                baseData = baseData,

                transactions = upcoming.transactions,
                shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
                onPayOrGet = onPayOrGet,
                onSkipTransaction = onSkipTransaction
            )
        }
    }
}

private fun LazyListScope.overdueSection(
    baseData: AppBaseData,

    overdue: LegacyDueSection?,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (LegacyTransaction) -> Unit,
    onSkipTransaction: (LegacyTransaction) -> Unit,
    onSkipAllTransactions: (List<LegacyTransaction>) -> Unit,
    setExpanded: (Boolean) -> Unit
) {
    if (overdue == null) return

    if (overdue.transactions.isNotEmpty()) {
        item(
            key = "overdue_section"
        ) {
            SectionDivider(
                expanded = overdue.expanded,
                setExpanded = setExpanded,
                title = stringResource(R.string.overdue),
                titleColor = Red,
                baseCurrency = baseData.baseCurrency,
                income = overdue.stats.income.toDouble(),
                expenses = overdue.stats.expense.abs().toDouble()
            )
        }

        if (overdue.expanded) {
            item {
                val isLightTheme = LegacyTheme.colors.pure == White
                IvyButton(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = stringResource(R.string.skip_all),
                    wrapContentMode = false,
                    backgroundGradient = if (isLightTheme) {
                        Gradient(White, White)
                    } else {
                        Gradient(
                            Black,
                            Black
                        )
                    },
                    textStyle = LegacyTheme.typo.b2.style(
                        color = if (isLightTheme) Black else White,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    onSkipAllTransactions(overdue.transactions)
                }
            }

            transactionItems(
                baseData = baseData,

                transactions = overdue.transactions,
                shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
                onPayOrGet = onPayOrGet,
                onSkipTransaction = onSkipTransaction
            )
        }
    }
}

private fun LazyListScope.transactionItems(
    baseData: AppBaseData,

    transactions: List<LegacyTransaction>,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (LegacyTransaction) -> Unit,
    onSkipTransaction: (LegacyTransaction) -> Unit,
) {
    items(
        items = transactions,
        key = { it.id }
    ) {
        val nav = navigation()
        TransactionCard(
            baseData = baseData,

            transaction = it,
            shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
            onPayOrGet = onPayOrGet,
            onSkipTransaction = onSkipTransaction
        ) { transaction ->
            onTransactionClick(
                nav = nav,
                transaction = transaction
            )
        }
    }
}

private fun LazyListScope.historySection(
    baseData: AppBaseData,

    history: List<TransactionHistoryItem>,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    dateDividerMarginTop: Dp? = null,

    onPayOrGet: (LegacyTransaction) -> Unit
) {
    if (history.isNotEmpty()) {
        items(
            items = history,
            key = {
                when (it) {
                    is LegacyTransaction -> it.id.toString()
                    is TransactionHistoryDateDivider -> it.date.toString()
                    else -> "unknown"
                }
            }
        ) {
            when (it) {
                is LegacyTransaction -> {
                    val nav = navigation()

                    TransactionCard(
                        baseData = baseData,

                        transaction = it,
                        shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
                        onPayOrGet = onPayOrGet
                    ) { transaction ->
                        onTransactionClick(
                            nav = nav,
                            transaction = transaction
                        )
                    }
                }

                is TransactionHistoryDateDivider -> {
                    HistoryDateDivider(
                        date = it.date,
                        spacerTop = dateDividerMarginTop
                            ?: if (it == history.firstOrNull()) 24.dp else 32.dp,
                        baseCurrency = baseData.baseCurrency,
                        income = it.income,
                        expenses = it.expenses
                    )
                }
            }
        }
    }
}

private fun onTransactionClick(
    nav: Navigation,
    transaction: LegacyTransaction
) {
    nav.navigateTo(
        EditTransactionScreen(
            initialTransactionId = transaction.id,
            type = transaction.type
        )
    )
}

@Composable
private fun LazyItemScope.NoTransactionsEmptyState(
    emptyStateTitle: String,
    emptyStateText: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        IvyIcon(
            icon = R.drawable.ic_notransactions,
            tint = Gray
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = emptyStateTitle,
            style = LegacyTheme.typo.b1.style(
                color = Gray,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = emptyStateText,
            style = LegacyTheme.typo.b2.style(
                color = Gray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )

        Spacer(Modifier.height(96.dp))
    }
}

private fun LazyListScope.scrollHackSpacer(
    history: List<TransactionHistoryItem>,
    upcoming: LegacyDueSection?,
    overdue: LegacyDueSection?,

    lastItemSpacer: Dp?,
) {
    item {
        if (lastItemSpacer != null) {
            Spacer(Modifier.height(lastItemSpacer))
        } else {
            // last spacer - scroll hack
            val transactionCount = history.size.plus(
                if (upcoming != null && upcoming.expanded) upcoming.transactions.size else 0
            ).plus(
                if (overdue != null && overdue.expanded) overdue.transactions.size else 0
            )
            if (transactionCount <= 5) {
                Spacer(Modifier.height(300.dp))
            } else {
                Spacer(Modifier.height(150.dp))
            }
        }
    }
}

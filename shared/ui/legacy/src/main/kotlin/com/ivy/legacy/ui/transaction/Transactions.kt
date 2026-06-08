package com.ivy.legacy.ui.transaction

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
import com.ivy.data.model.TransactionType
import com.ivy.data.model.TransactionHistoryDateDivider
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.TransactionHistoryTransaction
import com.ivy.data.model.Transaction
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.R
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyFixedColors.Black
import com.ivy.ui.theme.colors.IvyFixedColors.Gray
import com.ivy.ui.theme.colors.IvyFixedColors.Orange
import com.ivy.ui.theme.colors.IvyFixedColors.Red
import com.ivy.ui.theme.colors.IvyFixedColors.White
import java.util.UUID

fun LazyListScope.transactions(
    baseData: TransactionListData,

    upcoming: DueSection?,
    overdue: DueSection?,
    history: List<TransactionHistoryItem>,

    emptyStateTitle: String,
    emptyStateText: String,

    dateDividerMarginTop: Dp? = null,
    lastItemSpacer: Dp? = null,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (UUID) -> Unit,
    onTransactionClick: (UUID, TransactionType) -> Unit,
    onAccountClick: (UUID) -> Unit,
    onCategoryClick: (UUID) -> Unit,
    setUpcomingExpanded: (Boolean) -> Unit,
    setOverdueExpanded: (Boolean) -> Unit,
    onSkipTransaction: (UUID) -> Unit = {},
    onSkipAllTransactions: (List<UUID>) -> Unit = {}
) {
    upcomingSection(
        baseData = baseData,
        upcoming = upcoming,
        shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
        onPayOrGet = onPayOrGet,
        onSkipTransaction = onSkipTransaction,
        onTransactionClick = onTransactionClick,
        onAccountClick = onAccountClick,
        onCategoryClick = onCategoryClick,
        setExpanded = setUpcomingExpanded
    )

    overdueSection(
        baseData = baseData,
        overdue = overdue,

        onPayOrGet = onPayOrGet,
        onSkipTransaction = onSkipTransaction,
        onSkipAllTransactions = onSkipAllTransactions,
        shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
        onTransactionClick = onTransactionClick,
        onAccountClick = onAccountClick,
        onCategoryClick = onCategoryClick,
        setExpanded = setOverdueExpanded
    )

    historySection(
        baseData = baseData,

        history = history,
        shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
        dateDividerMarginTop = dateDividerMarginTop,
        onPayOrGet = onPayOrGet,
        onTransactionClick = onTransactionClick,
        onAccountClick = onAccountClick,
        onCategoryClick = onCategoryClick
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
    baseData: TransactionListData,

    upcoming: DueSection?,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (UUID) -> Unit,
    onSkipTransaction: (UUID) -> Unit,
    onTransactionClick: (UUID, TransactionType) -> Unit,
    onAccountClick: (UUID) -> Unit,
    onCategoryClick: (UUID) -> Unit,
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
                onSkipTransaction = onSkipTransaction,
                onTransactionClick = onTransactionClick,
                onAccountClick = onAccountClick,
                onCategoryClick = onCategoryClick
            )
        }
    }
}

private fun LazyListScope.overdueSection(
    baseData: TransactionListData,

    overdue: DueSection?,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (UUID) -> Unit,
    onSkipTransaction: (UUID) -> Unit,
    onSkipAllTransactions: (List<UUID>) -> Unit,
    onTransactionClick: (UUID, TransactionType) -> Unit,
    onAccountClick: (UUID) -> Unit,
    onCategoryClick: (UUID) -> Unit,
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
                GradientButton(
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
                    disabledBackgroundColor = LegacyTheme.colors.gray,
                    shape = LegacyTheme.shapes.rFull,
                    textStyle = LegacyTheme.typo.b2.copy(
                        color = if (isLightTheme) Black else White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    ),
                    iconTint = White,
                ) {
                    onSkipAllTransactions(overdue.transactions.map { it.id.value })
                }
            }

            transactionItems(
                baseData = baseData,

                transactions = overdue.transactions,
                shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
                onPayOrGet = onPayOrGet,
                onSkipTransaction = onSkipTransaction,
                onTransactionClick = onTransactionClick,
                onAccountClick = onAccountClick,
                onCategoryClick = onCategoryClick
            )
        }
    }
}

private fun LazyListScope.transactionItems(
    baseData: TransactionListData,

    transactions: List<Transaction>,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (UUID) -> Unit,
    onSkipTransaction: (UUID) -> Unit,
    onTransactionClick: (UUID, TransactionType) -> Unit,
    onAccountClick: (UUID) -> Unit,
    onCategoryClick: (UUID) -> Unit,
) {
    items(
        items = transactions,
        key = { it.id.value }
    ) {
        TransactionCard(
            baseData = baseData,

            transaction = it,
            shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
            onPayOrGet = onPayOrGet,
            onSkipTransaction = onSkipTransaction,
            onAccountClick = onAccountClick,
            onCategoryClick = onCategoryClick,
            onClick = onTransactionClick
        )
    }
}

private fun LazyListScope.historySection(
    baseData: TransactionListData,

    history: List<TransactionHistoryItem>,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    dateDividerMarginTop: Dp? = null,

    onPayOrGet: (UUID) -> Unit,
    onTransactionClick: (UUID, TransactionType) -> Unit,
    onAccountClick: (UUID) -> Unit,
    onCategoryClick: (UUID) -> Unit
) {
    if (history.isNotEmpty()) {
        items(
            items = history,
            key = {
                when (it) {
                    is TransactionHistoryTransaction -> it.transaction.id.value.toString()
                    is TransactionHistoryDateDivider -> it.date.toString()
                    else -> "unknown"
                }
            }
        ) {
            when (it) {
                is TransactionHistoryTransaction -> {
                    TransactionCard(
                        baseData = baseData,

                        transaction = it.transaction,
                        tags = it.tags,
                        shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
                        onPayOrGet = onPayOrGet,
                        onAccountClick = onAccountClick,
                        onCategoryClick = onCategoryClick,
                        onClick = onTransactionClick
                    )
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

        ResourceIcon(
            icon = R.drawable.ic_notransactions,
            tint = Gray
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = emptyStateTitle,
            style = LegacyTheme.typo.b1.copy(
                color = Gray,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = emptyStateText,
            style = LegacyTheme.typo.b2.copy(
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
    upcoming: DueSection?,
    overdue: DueSection?,

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

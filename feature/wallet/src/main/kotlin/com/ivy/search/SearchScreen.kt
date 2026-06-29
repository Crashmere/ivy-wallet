package com.ivy.search

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivy.data.model.Category
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Tag
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionHistoryDateDivider
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.TransactionHistoryTransaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getFromValue
import com.ivy.ui.money.AmountCurrencyB1
import com.ivy.ui.search.SearchInput
import com.ivy.ui.transaction.TransactionListAccount
import com.ivy.ui.transaction.TransactionListCategory
import com.ivy.ui.transaction.TransactionListData
import com.ivy.ui.transaction.TransactionListHistoryDateDivider
import com.ivy.ui.transaction.TransactionListHistoryItem
import com.ivy.ui.transaction.TransactionListHistoryTransaction
import com.ivy.ui.transaction.TransactionListTag
import com.ivy.ui.transaction.TransactionListTransaction
import com.ivy.ui.transaction.TransactionListTransactionType
import com.ivy.ui.transaction.transactions
import com.ivy.ui.compose.densityScope
import com.ivy.ui.compose.keyboardOnlyWindowInsets
import com.ivy.ui.platform.keyboardVisibleState
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.animation.DURATION_MODAL_ANIM
import com.ivy.ui.theme.colors.IvyFixedColors

@Composable
fun SearchScreen() {
    val viewModel: SearchViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    SearchUi(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun SearchUi(
    uiState: SearchState,
    onEvent: (SearchEvent) -> Unit
) {
    val nav = navigation()

    val (totalIncome, totalExpenses) = remember(uiState.transactions) {
        var income = 0.0
        var expenses = 0.0
        uiState.transactions.forEach { item ->
            if (item is TransactionHistoryDateDivider) {
                income += item.income
                expenses += item.expenses
            }
        }
        income to expenses
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Spacer(Modifier.height(24.dp))

        val listState = rememberLazyListState()

        var searchQueryTextFieldValue by remember {
            mutableStateOf(selectEndTextFieldValue(uiState.searchQuery))
        }

        SearchInput(
            searchQueryTextFieldValue = searchQueryTextFieldValue,
            hint = stringResource(R.string.search_transactions),
            showClearIcon = searchQueryTextFieldValue.text.isNotEmpty(),
            onSetSearchQueryTextField = {
                searchQueryTextFieldValue = it
                onEvent(SearchEvent.Search(it.text))
            }
        )

        if (uiState.transactions.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            SearchResultsSummary(
                income = totalIncome,
                expenses = totalExpenses,
                baseCurrency = uiState.baseCurrency
            )
        }

        LaunchedEffect(uiState.transactions) {
            // scroll to top when transactions are changed
            listState.animateScrollToItem(index = 0, scrollOffset = 0)
        }

        Spacer(Modifier.height(16.dp))
        val emptyStateTitle = stringResource(R.string.no_transactions)
        val emptyStateText = stringResource(
            R.string.no_transactions_for_query,
            searchQueryTextFieldValue.text
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState

        ) {
            transactions(
                baseData = TransactionListData(
                    baseCurrency = uiState.baseCurrency,
                    accounts = uiState.accounts
                        .map { it.toTransactionListAccount() },
                    categories = uiState.categories.map { it.toTransactionListCategory() }
                ),
                upcoming = null,
                setUpcomingExpanded = { },
                overdue = null,
                setOverdueExpanded = { },
                history = uiState.transactions.map { it.toTransactionListHistoryItem() },
                onPayOrGet = { },
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
                emptyStateTitle = emptyStateTitle,
                emptyStateText = emptyStateText,
                dateDividerMarginTop = 16.dp,
                shouldShowAccountSpecificColorInTransactions = uiState.shouldShowAccountSpecificColorInTransactions
            )

            item {
                val keyboardVisible by keyboardVisibleState()
                val keyboardShownInsetDp by animateDpAsState(
                    targetValue = densityScope {
                        if (keyboardVisible) keyboardOnlyWindowInsets().bottom.toDp() else 0.dp
                    },
                    animationSpec = tween(DURATION_MODAL_ANIM)
                )

                Spacer(Modifier.height(keyboardShownInsetDp))
                // add keyboard height margin at bottom so the list can scroll to bottom
            }
        }
    }
}

@Composable
private fun SearchResultsSummary(
    income: Double,
    expenses: Double,
    baseCurrency: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, SearchTheme.colors.gray, SearchTheme.shapes.r4)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SummaryAmount(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            label = stringResource(R.string.income_uppercase),
            amount = income,
            currency = baseCurrency,
            amountColor = IvyFixedColors.Green
        )

        Spacer(Modifier.width(12.dp))

        SummaryAmount(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
            label = stringResource(R.string.expenses_uppercase),
            amount = expenses,
            currency = baseCurrency,
            amountColor = SearchTheme.colors.pureInverse
        )
    }
}

@Composable
private fun SummaryAmount(
    label: String,
    amount: Double,
    currency: String,
    amountColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment
    ) {
        Text(
            text = label,
            style = SearchTheme.typo.c.copy(
                color = SearchTheme.colors.gray
            )
        )

        Spacer(Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            AmountCurrencyB1(
                amount = amount,
                currency = currency,
                textColor = amountColor
            )
        }
    }
}

private fun TransactionListTransactionType.toRouteType(): TransactionRouteType {
    return TransactionRouteType.valueOf(name)
}

private fun SearchAccount.toTransactionListAccount() = TransactionListAccount(
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

package com.ivy.search

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivy.data.model.Category
import com.ivy.data.model.TransactionHistoryDateDivider
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.TransactionHistoryTransaction
import com.ivy.data.model.TransactionType
import com.ivy.ui.search.SearchInput
import com.ivy.legacy.ui.transaction.TransactionListAccount
import com.ivy.legacy.ui.transaction.TransactionListCategory
import com.ivy.legacy.ui.transaction.TransactionListData
import com.ivy.legacy.ui.transaction.TransactionListHistoryDateDivider
import com.ivy.legacy.ui.transaction.TransactionListHistoryItem
import com.ivy.legacy.ui.transaction.TransactionListHistoryTransaction
import com.ivy.legacy.ui.transaction.transactions
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

private fun TransactionType.toRouteType(): TransactionRouteType {
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
            transaction = transaction,
            tags = tags,
        )

        is TransactionHistoryDateDivider -> TransactionListHistoryDateDivider(
            date = date,
            income = income,
            expenses = expenses,
        )

        else -> error("Unsupported transaction history item: ${this::class.simpleName}")
    }
}

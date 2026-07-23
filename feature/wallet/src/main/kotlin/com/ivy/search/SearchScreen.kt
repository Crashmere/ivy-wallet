package com.ivy.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.ivy.data.model.currency.format
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SearchInput(
                    searchQueryTextFieldValue = searchQueryTextFieldValue,
                    hint = stringResource(R.string.search_transactions),
                    showClearIcon = searchQueryTextFieldValue.text.isNotEmpty(),
                    onSetSearchQueryTextField = {
                        searchQueryTextFieldValue = it
                        onEvent(SearchEvent.Search(it.text))
                    }
                )
            }

            Text(
                modifier = Modifier
                    .clickable { nav.back() }
                    .padding(start = 4.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
                text = "取消",
                style = SearchTheme.typo.b2.copy(
                    color = SearchTheme.colors.pureInverse
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        var expandedFilter by remember { mutableStateOf<SearchFilterDim?>(null) }
        SearchFilterBar(
            uiState = uiState,
            expanded = expandedFilter,
            onToggleExpand = { dim ->
                expandedFilter = if (expandedFilter == dim) null else dim
            },
            onEvent = onEvent,
        )

        val resultCount = remember(uiState.transactions) {
            uiState.transactions.count { it is TransactionHistoryTransaction }
        }

        if (uiState.transactions.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            SearchResultsSummary(
                count = resultCount,
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

private enum class SearchFilterDim { CATEGORY, ACCOUNT, TAG, TIME }

private val searchTimeFilterOptions = listOf(
    "全部时间" to SearchTimeFilter.ALL,
    "本月" to SearchTimeFilter.THIS_MONTH,
    "上月" to SearchTimeFilter.LAST_MONTH,
    "近3个月" to SearchTimeFilter.LAST_3_MONTHS,
    "今年" to SearchTimeFilter.THIS_YEAR,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchFilterBar(
    uiState: SearchState,
    expanded: SearchFilterDim?,
    onToggleExpand: (SearchFilterDim) -> Unit,
    onEvent: (SearchEvent) -> Unit,
) {
    val categoryCount = uiState.selectedCategoryIds.size +
            if (uiState.uncategorizedSelected) 1 else 0
    val accountCount = uiState.selectedAccountIds.size
    val tagCount = uiState.selectedTagIds.size
    val timeActive = uiState.timeFilter != SearchTimeFilter.ALL
    val anyActive = categoryCount > 0 || accountCount > 0 || tagCount > 0 || timeActive

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterEntryChip(
                label = stringResource(R.string.categories),
                count = categoryCount,
                expanded = expanded == SearchFilterDim.CATEGORY,
                onClick = { onToggleExpand(SearchFilterDim.CATEGORY) },
            )
            Spacer(Modifier.width(8.dp))
            FilterEntryChip(
                label = stringResource(R.string.accounts),
                count = accountCount,
                expanded = expanded == SearchFilterDim.ACCOUNT,
                onClick = { onToggleExpand(SearchFilterDim.ACCOUNT) },
            )
            Spacer(Modifier.width(8.dp))
            FilterEntryChip(
                label = "标签",
                count = tagCount,
                expanded = expanded == SearchFilterDim.TAG,
                onClick = { onToggleExpand(SearchFilterDim.TAG) },
            )
            Spacer(Modifier.width(8.dp))
            FilterEntryChip(
                label = "时间",
                count = if (timeActive) 1 else 0,
                expanded = expanded == SearchFilterDim.TIME,
                onClick = { onToggleExpand(SearchFilterDim.TIME) },
            )
            if (anyActive) {
                Spacer(Modifier.width(8.dp))
                Text(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onEvent(SearchEvent.ClearFilters) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    text = "清除",
                    style = SearchTheme.typo.c.copy(color = SearchTheme.colors.gray)
                )
            }
        }

        AnimatedVisibility(visible = expanded == SearchFilterDim.CATEGORY) {
            FilterPanel {
                uiState.categories.forEach { category ->
                    FilterChip(
                        text = category.name.value,
                        selected = category.id.value in uiState.selectedCategoryIds,
                        onClick = { onEvent(SearchEvent.ToggleCategory(category.id.value)) }
                    )
                }
                FilterChip(
                    text = "无类别",
                    selected = uiState.uncategorizedSelected,
                    onClick = { onEvent(SearchEvent.ToggleUncategorized) }
                )
            }
        }

        AnimatedVisibility(visible = expanded == SearchFilterDim.ACCOUNT) {
            FilterPanel {
                uiState.accounts.forEach { account ->
                    FilterChip(
                        text = account.name,
                        selected = account.id in uiState.selectedAccountIds,
                        onClick = { onEvent(SearchEvent.ToggleAccount(account.id)) }
                    )
                }
            }
        }

        AnimatedVisibility(visible = expanded == SearchFilterDim.TAG) {
            if (uiState.tags.isEmpty()) {
                Text(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    text = "暂无标签",
                    style = SearchTheme.typo.c.copy(color = SearchTheme.colors.gray)
                )
            } else {
                FilterPanel {
                    uiState.tags.forEach { tag ->
                        FilterChip(
                            text = "#${tag.name.value}",
                            selected = tag.id.value in uiState.selectedTagIds,
                            onClick = { onEvent(SearchEvent.ToggleTag(tag.id.value)) }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = expanded == SearchFilterDim.TIME) {
            FilterPanel {
                searchTimeFilterOptions.forEach { (label, filter) ->
                    FilterChip(
                        text = label,
                        selected = uiState.timeFilter == filter,
                        onClick = { onEvent(SearchEvent.SetTimeFilter(filter)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterPanel(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
private fun FilterEntryChip(
    label: String,
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val active = count > 0
    val caret = if (expanded) " ▲" else " ▼"
    val text = if (count > 0) "$label · $count$caret" else "$label$caret"
    Text(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                color = if (active) SearchTheme.colors.pureInverse.copy(alpha = 0.08f) else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = if (active || expanded) 2.dp else 1.dp,
                color = if (active || expanded) SearchTheme.colors.pureInverse else SearchTheme.colors.medium,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        text = text,
        maxLines = 1,
        style = SearchTheme.typo.c.copy(
            color = SearchTheme.colors.pureInverse,
            fontWeight = FontWeight.SemiBold,
        )
    )
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                color = if (selected) SearchTheme.colors.pureInverse else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = if (selected) SearchTheme.colors.pureInverse else SearchTheme.colors.medium,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = text,
        maxLines = 1,
        style = SearchTheme.typo.c.copy(
            color = if (selected) SearchTheme.colors.pure else SearchTheme.colors.pureInverse,
            fontWeight = FontWeight.SemiBold,
        )
    )
}

@Composable
private fun SearchResultsSummary(
    count: Int,
    income: Double,
    expenses: Double,
    baseCurrency: String,
) {
    val accent = IvyFixedColors.Green
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(SearchTheme.shapes.r4)
            .background(accent.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.4f), SearchTheme.shapes.r4)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "找到 $count 笔",
            style = SearchTheme.typo.b2.copy(
                color = accent,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.weight(1f))

        SummaryInline(
            label = stringResource(R.string.income),
            amount = income,
            currency = baseCurrency,
            amountColor = accent
        )

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(16.dp)
                .background(SearchTheme.colors.gray)
        )

        Spacer(Modifier.width(10.dp))

        SummaryInline(
            label = stringResource(R.string.expense),
            amount = expenses,
            currency = baseCurrency,
            amountColor = SearchTheme.colors.pureInverse
        )
    }
}

@Composable
private fun SummaryInline(
    label: String,
    amount: Double,
    currency: String,
    amountColor: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = SearchTheme.typo.c.copy(
                color = SearchTheme.colors.gray
            )
        )

        Spacer(Modifier.width(4.dp))

        Text(
            text = amount.format(currency),
            style = SearchTheme.typo.c.copy(
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        )
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

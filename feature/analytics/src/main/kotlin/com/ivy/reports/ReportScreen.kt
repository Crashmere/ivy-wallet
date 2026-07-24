package com.ivy.reports

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ivy.data.model.Account
import com.ivy.data.model.Category
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Tag
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionHistoryDateDivider
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.TransactionHistoryTransaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.currency.format
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getFromValue
import com.ivy.ui.R
import com.ivy.ui.compose.OutlinedPillButton
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.modal.ChoosePeriodModal
import com.ivy.ui.money.AmountCurrencyB1
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.period.LocalPeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.period.displayShort
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.ui.platform.fileSharer
import com.ivy.ui.theme.colors.IvyFixedColors
import com.ivy.ui.theme.colors.toComposeColor
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
import kotlin.math.roundToInt

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.ReportScreen() {
    ReportScreenContent(embedded = false)
}

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.ReportTab() {
    ReportScreenContent(embedded = true)
}

@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.ReportScreenContent(embedded: Boolean) {
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

    UI(state = state, onEvent = viewModel::onEvent, embedded = embedded)
}

@OptIn(ExperimentalLayoutApi::class)
@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    state: ReportState,
    onEvent: (ReportEvent) -> Unit,
    embedded: Boolean,
) {
    val nav = navigation()
    val datePicker = LocalDatePicker.current
    val listState = rememberLazyListState()
    var periodModal: TimePeriod? by remember { mutableStateOf(null) }
    var filtersExpanded by remember { mutableStateOf(false) }

    val activeFilterCount = state.selectedCategoryIds.size +
            state.selectedAccountIds.size +
            state.selectedTagIds.size +
            (if (state.uncategorizedSelected) 1 else 0) +
            (if (!state.includeIncome) 1 else 0) +
            (if (!state.includeExpense) 1 else 0) +
            (if (state.includeTransfer) 1 else 0) +
            state.includeKeywords.size +
            state.excludeKeywords.size +
            (if (state.amountMin != null || state.amountMax != null) 1 else 0)

    if (state.loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ReportsTheme.colors.pure.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.generating_report),
                style = ReportsTheme.typo.b1.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = ReportsTheme.colors.orange,
                )
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        state = listState
    ) {
        item {
            Spacer(Modifier.height(if (embedded) 8.dp else 16.dp))

            ReportHeaderRow(
                embedded = embedded,
                onBack = { nav.back() },
                onExport = { onEvent(ReportEvent.OnExport) },
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthSelector(
                    period = state.period,
                    onPrevious = { onEvent(ReportEvent.OnPreviousMonth) },
                    onNext = { onEvent(ReportEvent.OnNextMonth) },
                    onClick = { periodModal = state.period }
                )
            }

            Spacer(Modifier.height(12.dp))

            FilterBar(
                activeCount = activeFilterCount,
                expanded = filtersExpanded,
                sortOrder = state.sortOrder,
                onToggleExpand = { filtersExpanded = !filtersExpanded },
                onClear = { onEvent(ReportEvent.ClearFilters) },
                onToggleSort = { onEvent(ReportEvent.ToggleSortOrder) },
            )

            AnimatedVisibility(
                visible = filtersExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    FilterSection(state = state, onEvent = onEvent)
                }
            }

            Spacer(Modifier.height(16.dp))

            SummaryStrip(
                count = state.matchingCount,
                income = state.income,
                expenses = state.expenses,
                baseCurrency = state.baseCurrency
            )

            DonutChartSection(state = state)

            DailyTrendSection(state = state)

            CategoryBreakdownSection(state = state)

            Spacer(Modifier.height(16.dp))
        }

        transactions(
            baseData = TransactionListData(
                baseCurrency = state.baseCurrency,
                accounts = state.allAccounts.map { it.toTransactionListAccount() },
                categories = state.allCategories.map { it.toTransactionListCategory() }
            ),
            upcoming = null,
            setUpcomingExpanded = {},
            overdue = null,
            setOverdueExpanded = {},
            history = state.matchingTransactions.map { it.toTransactionListHistoryItem() },
            onPayOrGet = {},
            onTransactionClick = { transactionId, transactionType ->
                nav.navigateTo(
                    EditTransactionScreen(
                        initialTransactionId = transactionId,
                        type = transactionType.toRouteType()
                    )
                )
            },
            onAccountClick = {},
            onCategoryClick = {},
            emptyStateTitle = "无匹配交易",
            emptyStateText = "当前时间范围或筛选条件下没有匹配的交易。",
            dateDividerMarginTop = 16.dp,
            shouldShowAccountSpecificColorInTransactions =
            state.shouldShowAccountColorsInTransactions,
        )

        item {
            Spacer(Modifier.height(140.dp))
        }
    }

    ChoosePeriodModal(
        modal = periodModal,
        dismiss = { periodModal = null },
        saveSelectedPeriod = {},
        pickDate = { minDate, maxDate, initialDate, onDatePicked ->
            datePicker.pickDate(
                minDate = minDate,
                maxDate = maxDate,
                initialDate = initialDate,
                onDatePicked = onDatePicked
            )
        },
        onPeriodSelected = { onEvent(ReportEvent.OnSelectPeriod(it)) }
    )
}

// ─── Header ──────────────────────────────────────────────────

@Composable
private fun ReportHeaderRow(
    embedded: Boolean,
    onBack: () -> Unit,
    onExport: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (embedded) 16.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!embedded) {
            HeaderIconButton(
                icon = R.drawable.ic_back,
                contentDescription = "Back",
                onClick = onBack
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = stringResource(R.string.reports),
            style = ReportsTheme.typo.h2.copy(
                color = ReportsTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start,
            )
        )
        Spacer(Modifier.weight(1f))
        HeaderIconButton(
            icon = R.drawable.ic_export_csv,
            contentDescription = "Export",
            onClick = onExport
        )
    }
}

@Composable
private fun HeaderIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        ResourceIcon(
            icon = icon,
            tint = ReportsTheme.colors.pureInverse,
            contentDescription = contentDescription
        )
    }
}

// ─── Sort Toggle ─────────────────────────────────────────────

@Composable
private fun SortToggleButton(
    sortOrder: SortOrder,
    onToggle: () -> Unit,
) {
    OutlinedPillButton(
        text = if (sortOrder == SortOrder.TIME) "时间" else "金额",
        iconStart = R.drawable.ic_sort_by_alpha_24,
        shape = ReportsTheme.shapes.rFull,
        solidBackground = true,
        backgroundColor = ReportsTheme.colors.pure,
        iconTint = ReportsTheme.colors.pureInverse,
        borderColor = ReportsTheme.colors.medium,
        textStyle = ReportsTheme.typo.b2.copy(
            fontWeight = FontWeight.Bold,
            color = ReportsTheme.colors.pureInverse,
        ),
        padding = 8.dp,
    ) {
        onToggle()
    }
}

// ─── Month Selector ──────────────────────────────────────────

@Composable
private fun MonthSelector(
    period: TimePeriod,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val periodState = LocalPeriodState.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MonthNavArrow(
            icon = R.drawable.ic_back,
            contentDescription = "Previous month",
            onClick = onPrevious
        )
        Spacer(Modifier.width(4.dp))
        OutlinedPillButton(
            text = period.displayShort(periodState.startDayOfMonth),
            iconStart = R.drawable.ic_calendar,
            shape = ReportsTheme.shapes.rFull,
            backgroundColor = ReportsTheme.colors.pure,
            iconTint = ReportsTheme.colors.pureInverse,
            borderColor = ReportsTheme.colors.medium,
            textStyle = ReportsTheme.typo.b2.copy(
                fontWeight = FontWeight.Bold,
                color = ReportsTheme.colors.pureInverse,
            ),
            onClick = onClick
        )
        Spacer(Modifier.width(4.dp))
        MonthNavArrow(
            icon = R.drawable.ic_arrow_right,
            contentDescription = "Next month",
            onClick = onNext
        )
    }
}

@Composable
private fun MonthNavArrow(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        ResourceIcon(
            icon = icon,
            tint = ReportsTheme.colors.pureInverse,
            contentDescription = contentDescription
        )
    }
}

// ─── Filter Section ──────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    state: ReportState,
    onEvent: (ReportEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SubLabelWithSelectAll(
            label = "类型",
            onSelectAll = { onEvent(ReportEvent.SelectAllTypes) }
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                text = "收入",
                selected = state.includeIncome,
                onClick = { onEvent(ReportEvent.ToggleIncome) }
            )
            FilterChip(
                text = "支出",
                selected = state.includeExpense,
                onClick = { onEvent(ReportEvent.ToggleExpense) }
            )
            FilterChip(
                text = "转账",
                selected = state.includeTransfer,
                onClick = { onEvent(ReportEvent.ToggleTransfer) }
            )
        }

        if (state.filterCategories.isNotEmpty() || state.filterHasUncategorized) {
            Spacer(Modifier.height(12.dp))
            SubLabelWithSelectAll(
                label = "类别",
                onSelectAll = { onEvent(ReportEvent.SelectAllCategories) }
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.filterCategories.forEach { category ->
                    FilterChip(
                        text = category.name.value,
                        selected = category.id.value in state.selectedCategoryIds,
                        onClick = {
                            onEvent(ReportEvent.ToggleCategoryFilter(category.id.value))
                        }
                    )
                }
                if (state.filterHasUncategorized) {
                    FilterChip(
                        text = "无类别",
                        selected = state.uncategorizedSelected,
                        onClick = { onEvent(ReportEvent.ToggleUncategorizedFilter) }
                    )
                }
            }
        }

        if (state.filterAccounts.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SubLabelWithSelectAll(
                label = "账户",
                onSelectAll = { onEvent(ReportEvent.SelectAllAccounts) }
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.filterAccounts.forEach { account ->
                    FilterChip(
                        text = account.name.value,
                        selected = account.id.value in state.selectedAccountIds,
                        onClick = {
                            onEvent(ReportEvent.ToggleAccountFilter(account.id.value))
                        }
                    )
                }
            }
        }

        if (state.filterTags.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SubLabelWithSelectAll(
                label = "标签",
                onSelectAll = { onEvent(ReportEvent.SelectAllTags) }
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.filterTags.forEach { tag ->
                    FilterChip(
                        text = "#${tag.name.value}",
                        selected = tag.id.value in state.selectedTagIds,
                        onClick = { onEvent(ReportEvent.ToggleTagFilter(tag.id.value)) }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdvancedFilterToggle(
                expanded = state.advancedExpanded,
                onToggle = { onEvent(ReportEvent.ToggleAdvanced) }
            )
        }

        AnimatedVisibility(
            visible = state.advancedExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            AdvancedFilterContent(state = state, onEvent = onEvent)
        }
    }
}

@Composable
private fun FilterBar(
    activeCount: Int,
    expanded: Boolean,
    sortOrder: SortOrder,
    onToggleExpand: () -> Unit,
    onClear: () -> Unit,
    onToggleSort: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val active = expanded || activeCount > 0
        Row(
            modifier = Modifier
                .clip(ReportsTheme.shapes.rFull)
                .background(
                    color = if (expanded) ReportsTheme.colors.pureInverse else Color.Transparent,
                    shape = ReportsTheme.shapes.rFull
                )
                .border(
                    width = if (active) 2.dp else 1.dp,
                    color = if (active) ReportsTheme.colors.pureInverse else ReportsTheme.colors.medium,
                    shape = ReportsTheme.shapes.rFull
                )
                .clickable(onClick = onToggleExpand)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ResourceIcon(
                icon = R.drawable.ic_filter_xs,
                tint = if (expanded) ReportsTheme.colors.pure else ReportsTheme.colors.pureInverse,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (activeCount > 0) "筛选 · $activeCount" else "筛选",
                style = ReportsTheme.typo.c.copy(
                    color = if (expanded) ReportsTheme.colors.pure else ReportsTheme.colors.pureInverse,
                    fontWeight = FontWeight.Bold,
                )
            )
        }

        if (activeCount > 0) {
            Spacer(Modifier.width(8.dp))
            Text(
                modifier = Modifier
                    .clip(ReportsTheme.shapes.rFull)
                    .clickable(onClick = onClear)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                text = "清除",
                style = ReportsTheme.typo.c.copy(color = ReportsTheme.colors.gray)
            )
        }

        Spacer(Modifier.weight(1f))

        SortToggleButton(
            sortOrder = sortOrder,
            onToggle = onToggleSort
        )
    }
}

@Composable
private fun AdvancedFilterToggle(
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(ReportsTheme.shapes.rFull)
            .border(1.dp, ReportsTheme.colors.medium, ReportsTheme.shapes.rFull)
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ResourceIcon(
            icon = R.drawable.ic_filter_xs,
            tint = ReportsTheme.colors.gray,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (expanded) "收起高级筛选" else "更多筛选",
            style = ReportsTheme.typo.c.copy(
                color = ReportsTheme.colors.gray,
                fontWeight = FontWeight.SemiBold,
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdvancedFilterContent(
    state: ReportState,
    onEvent: (ReportEvent) -> Unit,
) {
    var showIncludeKeywordModal by remember { mutableStateOf(false) }
    var showExcludeKeywordModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp)
            .border(1.dp, ReportsTheme.colors.medium, ReportsTheme.shapes.r4)
            .padding(16.dp)
    ) {
        // Include keywords
        Text(
            text = "包含关键词",
            style = ReportsTheme.typo.c.copy(
                color = ReportsTheme.colors.gray,
                fontWeight = FontWeight.Bold,
            )
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.includeKeywords.forEach { kw ->
                KeywordPill(
                    text = kw,
                    onRemove = { onEvent(ReportEvent.RemoveIncludeKeyword(kw)) }
                )
            }
            AddKeywordChip { showIncludeKeywordModal = true }
        }

        Spacer(Modifier.height(16.dp))

        // Exclude keywords
        Text(
            text = "排除关键词",
            style = ReportsTheme.typo.c.copy(
                color = ReportsTheme.colors.gray,
                fontWeight = FontWeight.Bold,
            )
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.excludeKeywords.forEach { kw ->
                KeywordPill(
                    text = kw,
                    onRemove = { onEvent(ReportEvent.RemoveExcludeKeyword(kw)) }
                )
            }
            AddKeywordChip { showExcludeKeywordModal = true }
        }

        Spacer(Modifier.height(16.dp))

        // Amount range slider
        Text(
            text = "金额范围",
            style = ReportsTheme.typo.c.copy(
                color = ReportsTheme.colors.gray,
                fontWeight = FontWeight.Bold,
            )
        )
        Spacer(Modifier.height(8.dp))

        AmountRangeSlider(state = state, onEvent = onEvent)
    }

    if (showIncludeKeywordModal) {
        KeywordInputDialog(
            title = "添加包含关键词",
            onConfirm = {
                onEvent(ReportEvent.AddIncludeKeyword(it))
                showIncludeKeywordModal = false
            },
            onDismiss = { showIncludeKeywordModal = false }
        )
    }

    if (showExcludeKeywordModal) {
        KeywordInputDialog(
            title = "添加排除关键词",
            onConfirm = {
                onEvent(ReportEvent.AddExcludeKeyword(it))
                showExcludeKeywordModal = false
            },
            onDismiss = { showExcludeKeywordModal = false }
        )
    }
}

@Composable
private fun AmountRangeSlider(
    state: ReportState,
    onEvent: (ReportEvent) -> Unit,
) {
    val rangeMin = state.amountRangeMin
    val rangeMax = state.amountRangeMax
    if (rangeMin >= rangeMax) {
        Text(
            text = "当前筛选结果的金额范围不足以进行滑动筛选",
            style = ReportsTheme.typo.c.copy(color = ReportsTheme.colors.gray)
        )
        return
    }

    val currentMin = state.amountMin ?: rangeMin
    val currentMax = state.amountMax ?: rangeMax

    var sliderValues by remember(rangeMin, rangeMax, state.amountMin, state.amountMax) {
        mutableStateOf(currentMin..currentMax)
    }

    var editingMin by remember { mutableStateOf(false) }
    var editingMax by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .clip(ReportsTheme.shapes.rFull)
                .clickable { editingMin = true }
                .border(1.dp, ReportsTheme.colors.medium, ReportsTheme.shapes.rFull)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            text = "%.0f".format(sliderValues.start),
            style = ReportsTheme.typo.c.copy(
                color = ReportsTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
            )
        )

        RangeSlider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            value = sliderValues,
            onValueChange = { sliderValues = it },
            onValueChangeFinished = {
                val newMin = if (sliderValues.start <= rangeMin) null else sliderValues.start
                val newMax = if (sliderValues.endInclusive >= rangeMax) null else sliderValues.endInclusive
                onEvent(ReportEvent.SetAmountRange(newMin, newMax))
            },
            valueRange = rangeMin..rangeMax,
            colors = SliderDefaults.colors(
                thumbColor = ReportsTheme.colors.pureInverse,
                activeTrackColor = ReportsTheme.colors.pureInverse,
                inactiveTrackColor = ReportsTheme.colors.medium,
            )
        )

        Text(
            modifier = Modifier
                .clip(ReportsTheme.shapes.rFull)
                .clickable { editingMax = true }
                .border(1.dp, ReportsTheme.colors.medium, ReportsTheme.shapes.rFull)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            text = "%.0f".format(sliderValues.endInclusive),
            style = ReportsTheme.typo.c.copy(
                color = ReportsTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
            )
        )
    }

    if (editingMin) {
        AmountInputDialog(
            title = "最小金额",
            initialValue = sliderValues.start,
            onConfirm = { value ->
                val clamped = value.coerceIn(rangeMin, sliderValues.endInclusive)
                sliderValues = clamped..sliderValues.endInclusive
                val newMin = if (clamped <= rangeMin) null else clamped
                onEvent(ReportEvent.SetAmountRange(newMin, state.amountMax))
                editingMin = false
            },
            onDismiss = { editingMin = false }
        )
    }

    if (editingMax) {
        AmountInputDialog(
            title = "最大金额",
            initialValue = sliderValues.endInclusive,
            onConfirm = { value ->
                val clamped = value.coerceIn(sliderValues.start, rangeMax)
                sliderValues = sliderValues.start..clamped
                val newMax = if (clamped >= rangeMax) null else clamped
                onEvent(ReportEvent.SetAmountRange(state.amountMin, newMax))
                editingMax = false
            },
            onDismiss = { editingMax = false }
        )
    }
}

@Composable
private fun AmountInputDialog(
    title: String,
    initialValue: Float,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("%.0f".format(initialValue)) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = ReportsTheme.typo.b2.copy(
                    fontWeight = FontWeight.Bold,
                    color = ReportsTheme.colors.pureInverse,
                )
            )
        },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                )
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    text.toFloatOrNull()?.let { onConfirm(it) } ?: onDismiss()
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun KeywordInputDialog(
    title: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = ReportsTheme.typo.b2.copy(
                    fontWeight = FontWeight.Bold,
                    color = ReportsTheme.colors.pureInverse,
                )
            )
        },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    if (text.isNotBlank()) onConfirm(text.trim()) else onDismiss()
                }
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun KeywordPill(
    text: String,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(ReportsTheme.colors.medium, CircleShape)
            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = ReportsTheme.typo.c.copy(
                color = ReportsTheme.colors.pureInverse,
                fontWeight = FontWeight.SemiBold,
            )
        )
        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onRemove)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            ResourceIcon(
                icon = R.drawable.ic_dismiss,
                tint = ReportsTheme.colors.gray,
            )
        }
    }
}

@Composable
private fun AddKeywordChip(onClick: () -> Unit) {
    Text(
        modifier = Modifier
            .clip(CircleShape)
            .border(1.dp, ReportsTheme.colors.medium, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        text = "+ 添加",
        style = ReportsTheme.typo.c.copy(
            color = ReportsTheme.colors.gray,
            fontWeight = FontWeight.SemiBold,
        )
    )
}

// ─── Shared components ───────────────────────────────────────

@Composable
private fun SubLabel(text: String) {
    Text(
        modifier = Modifier.padding(horizontal = 24.dp),
        text = text,
        style = ReportsTheme.typo.c.copy(color = ReportsTheme.colors.gray)
    )
}

@Composable
private fun SubLabelWithSelectAll(
    label: String,
    onSelectAll: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = ReportsTheme.typo.c.copy(color = ReportsTheme.colors.gray)
        )
        Spacer(Modifier.weight(1f))
        Text(
            modifier = Modifier
                .clip(ReportsTheme.shapes.rFull)
                .clickable(onClick = onSelectAll)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            text = "全选",
            style = ReportsTheme.typo.c.copy(
                color = ReportsTheme.colors.gray,
                fontWeight = FontWeight.SemiBold,
            )
        )
    }
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
                color = if (selected) ReportsTheme.colors.pureInverse else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = if (selected) ReportsTheme.colors.pureInverse else ReportsTheme.colors.medium,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = text,
        style = ReportsTheme.typo.c.copy(
            color = if (selected) ReportsTheme.colors.pure else ReportsTheme.colors.pureInverse,
            fontWeight = FontWeight.SemiBold,
        )
    )
}

// ─── Summary Strip ───────────────────────────────────────────

@Composable
private fun SummaryStrip(
    count: Int,
    income: Double,
    expenses: Double,
    baseCurrency: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, ReportsTheme.colors.gray, ReportsTheme.shapes.r4)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "匹配交易",
                style = ReportsTheme.typo.c.copy(color = ReportsTheme.colors.gray)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "$count 笔",
                style = ReportsTheme.typo.nB1.copy(
                    color = ReportsTheme.colors.pureInverse,
                    fontWeight = FontWeight.Bold,
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            SummaryAmount(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                label = "收入",
                amount = income,
                currency = baseCurrency,
                amountColor = IvyFixedColors.Green,
            )
            Spacer(Modifier.width(8.dp))
            SummaryAmount(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                label = "支出",
                amount = expenses,
                currency = baseCurrency,
                amountColor = ReportsTheme.colors.pureInverse,
            )
            Spacer(Modifier.width(8.dp))
            SummaryAmount(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                label = "净额",
                amount = income - expenses,
                currency = baseCurrency,
                amountColor = if (income - expenses >= 0) {
                    IvyFixedColors.Green
                } else {
                    ReportsTheme.colors.red
                },
            )
        }
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
            style = ReportsTheme.typo.c.copy(color = ReportsTheme.colors.gray)
        )
        Spacer(Modifier.height(4.dp))
        AmountCurrencyB1(
            amount = amount,
            currency = currency,
            textColor = amountColor
        )
    }
}

// ─── Donut Chart ─────────────────────────────────────────────

@Composable
private fun DonutChartSection(state: ReportState) {
    val showExpense = state.expenseByCategory.isNotEmpty()
    val items = if (showExpense) state.expenseByCategory else state.incomeByCategory
    val total = items.sumOf { it.amount }
    if (items.isEmpty() || total <= 0.0) return

    val maxSlices = 6
    val slices: List<CategoryBreakdownItem> = if (items.size > maxSlices) {
        val top = items.take(maxSlices - 1)
        val restAmount = items.drop(maxSlices - 1).sumOf { it.amount }
        top + CategoryBreakdownItem(name = "其他", colorArgb = null, amount = restAmount)
    } else {
        items
    }

    Spacer(Modifier.height(12.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(ReportsTheme.shapes.r4)
            .border(1.dp, ReportsTheme.colors.medium, ReportsTheme.shapes.r4)
            .padding(16.dp),
    ) {
        Text(
            text = if (showExpense) "支出构成" else "收入构成",
            style = ReportsTheme.typo.b2.copy(
                color = ReportsTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
            )
        )

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(136.dp),
                contentAlignment = Alignment.Center,
            ) {
                DonutCanvas(
                    slices = slices,
                    total = total,
                    modifier = Modifier.size(136.dp),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (showExpense) "总支出" else "总收入",
                        style = ReportsTheme.typo.c.copy(color = ReportsTheme.colors.gray)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = total.format(state.baseCurrency),
                        style = ReportsTheme.typo.b2.copy(
                            color = ReportsTheme.colors.pureInverse,
                            fontWeight = FontWeight.ExtraBold,
                        ),
                        maxLines = 1,
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                slices.forEachIndexed { index, item ->
                    if (index > 0) Spacer(Modifier.height(10.dp))
                    val color = item.colorArgb?.toComposeColor() ?: ReportsTheme.colors.gray
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = item.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = ReportsTheme.typo.c.copy(
                                color = ReportsTheme.colors.pureInverse,
                                fontWeight = FontWeight.SemiBold,
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${(item.amount / total * 100).roundToInt()}%",
                            style = ReportsTheme.typo.c.copy(
                                color = ReportsTheme.colors.gray,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutCanvas(
    slices: List<CategoryBreakdownItem>,
    total: Double,
    modifier: Modifier = Modifier,
) {
    val colors = slices.map { it.colorArgb?.toComposeColor() ?: ReportsTheme.colors.gray }
    val fractions = slices.map { (it.amount / total).toFloat() }
    val trackColor = ReportsTheme.colors.medium

    Canvas(modifier = modifier) {
        val strokeWidth = 26.dp.toPx()
        val inset = strokeWidth / 2f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)

        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth),
        )

        var start = -90f
        val gap = 3f
        fractions.forEachIndexed { index, fraction ->
            val sweep = fraction * 360f
            drawArc(
                color = colors[index],
                startAngle = start,
                sweepAngle = (sweep - gap).coerceAtLeast(0.5f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
            )
            start += sweep
        }
    }
}

// ─── Daily Trend ─────────────────────────────────────────────

@Composable
private fun DailyTrendSection(state: ReportState) {
    val showExpense = state.expenseByCategory.isNotEmpty()
    val bars = if (showExpense) state.expenseByDay else state.incomeByDay
    if (bars.isEmpty()) return
    val maxAmount = bars.maxOf { it.amount }
    if (maxAmount <= 0.0) return

    Spacer(Modifier.height(12.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(ReportsTheme.shapes.r4)
            .border(1.dp, ReportsTheme.colors.medium, ReportsTheme.shapes.r4)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (showExpense) "每日支出趋势" else "每日收入趋势",
                style = ReportsTheme.typo.b2.copy(
                    color = ReportsTheme.colors.pureInverse,
                    fontWeight = FontWeight.Bold,
                )
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "峰值 ${maxAmount.format(state.baseCurrency)}",
                style = ReportsTheme.typo.c.copy(color = ReportsTheme.colors.gray)
            )
        }

        Spacer(Modifier.height(14.dp))

        DailyBarChart(
            bars = bars,
            maxAmount = maxAmount,
            barColor = if (showExpense) ReportsTheme.colors.pureInverse else IvyFixedColors.Green,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        )

        Spacer(Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${bars.first().label}日",
                style = ReportsTheme.typo.c.copy(color = ReportsTheme.colors.gray)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${bars.last().label}日",
                style = ReportsTheme.typo.c.copy(color = ReportsTheme.colors.gray)
            )
        }
    }
}

@Composable
private fun DailyBarChart(
    bars: List<DailyBar>,
    maxAmount: Double,
    barColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val count = bars.size
        if (count == 0) return@Canvas
        val gap = if (count > 1) 3.dp.toPx() else 0f
        val barWidth = ((size.width - gap * (count - 1)) / count).coerceAtLeast(1f)
        val chartHeight = size.height

        bars.forEachIndexed { index, bar ->
            val barHeight = ((bar.amount / maxAmount).toFloat() * chartHeight)
                .coerceIn(0f, chartHeight)
            val x = index * (barWidth + gap)
            val y = chartHeight - barHeight
            drawRoundRect(
                color = if (bar.amount > 0.0) barColor else barColor.copy(alpha = 0.12f),
                topLeft = Offset(x, if (bar.amount > 0.0) y else chartHeight - 2.dp.toPx()),
                size = Size(barWidth, if (bar.amount > 0.0) barHeight else 2.dp.toPx()),
                cornerRadius = CornerRadius(barWidth / 3f, barWidth / 3f),
            )
        }
    }
}

// ─── Category Breakdown ──────────────────────────────────────

@Composable
private fun CategoryBreakdownSection(state: ReportState) {
    val showExpense = state.expenseByCategory.isNotEmpty()
    val items = if (showExpense) state.expenseByCategory else state.incomeByCategory
    val total = items.sumOf { it.amount }
    if (items.isEmpty() || total <= 0.0) return

    Spacer(Modifier.height(12.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(ReportsTheme.shapes.r4)
            .border(1.dp, ReportsTheme.colors.medium, ReportsTheme.shapes.r4)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (showExpense) "支出排行" else "收入排行",
                style = ReportsTheme.typo.b2.copy(
                    color = ReportsTheme.colors.pureInverse,
                    fontWeight = FontWeight.Bold,
                )
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${items.size} 类",
                style = ReportsTheme.typo.c.copy(color = ReportsTheme.colors.gray)
            )
        }

        items.forEach { item ->
            Spacer(Modifier.height(12.dp))
            BreakdownRow(
                item = item,
                fraction = (item.amount / total).toFloat(),
                baseCurrency = state.baseCurrency,
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    item: CategoryBreakdownItem,
    fraction: Float,
    baseCurrency: String,
) {
    val color = item.colorArgb?.toComposeColor() ?: ReportsTheme.colors.gray
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = item.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = ReportsTheme.typo.c.copy(
                    color = ReportsTheme.colors.pureInverse,
                    fontWeight = FontWeight.SemiBold,
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${(fraction * 100).roundToInt()}%",
                style = ReportsTheme.typo.c.copy(color = ReportsTheme.colors.gray)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = item.amount.format(baseCurrency),
                style = ReportsTheme.typo.c.copy(
                    color = ReportsTheme.colors.pureInverse,
                    fontWeight = FontWeight.Bold,
                )
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(ReportsTheme.shapes.rFull)
                .background(ReportsTheme.colors.medium)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                    .height(6.dp)
                    .clip(ReportsTheme.shapes.rFull)
                    .background(color)
            )
        }
    }
}

// ─── Data conversion helpers ─────────────────────────────────

private fun TransactionListTransactionType.toRouteType(): TransactionRouteType =
    TransactionRouteType.valueOf(name)

private fun Account.toTransactionListAccount() = TransactionListAccount(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
    currency = asset.code,
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

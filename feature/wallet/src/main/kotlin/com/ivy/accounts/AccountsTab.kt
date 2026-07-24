package com.ivy.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.categories.CategoriesScreenEvent
import com.ivy.categories.CategoriesScreenState
import com.ivy.categories.CategoriesViewModel
import com.ivy.categories.CategoryData
import com.ivy.categories.CreateCategoryModal
import com.ivy.categories.SortModal
import com.ivy.data.model.currency.format
import com.ivy.data.model.currency.shortenAmount
import com.ivy.data.model.currency.shouldShortAmount
import com.ivy.ui.compose.DraggableItem
import com.ivy.ui.compose.FilledIconButton
import com.ivy.ui.compose.OutlinedPillButton
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.compose.horizontalSwipeListener
import com.ivy.ui.compose.rememberDragDropState
import com.ivy.ui.compose.rememberSwipeListenerState
import com.ivy.ui.compose.thenIf
import com.ivy.ui.icon.ItemIconSDefaultIcon
import com.ivy.ui.modal.ChoosePeriodModal
import com.ivy.ui.money.BalanceRow
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.period.LocalPeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.period.displayShort
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.ui.theme.colors.IvyFixedColors.Green
import com.ivy.ui.theme.colors.IvyFixedColors.Red
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.ui.theme.colors.toComposeColor
import java.util.UUID
import kotlin.math.abs
import kotlin.math.absoluteValue

private const val HEADER_ITEMS_COUNT = 1

@Composable
fun BoxWithConstraintsScope.AccountsTab(
    onOpenHomeTab: () -> Unit,
    onAddAccount: () -> Unit,
) {
    val viewModel: AccountsViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    val categoriesViewModel: CategoriesViewModel = screenScopedViewModel()
    val categoriesState = categoriesViewModel.uiState()

    UI(
        state = uiState,
        categoriesState = categoriesState,
        onEvent = viewModel::onEvent,
        onCategoriesEvent = categoriesViewModel::onEvent,
        onOpenHomeTab = onOpenHomeTab,
        onAddAccount = onAddAccount,
    )
}
@Composable
private fun BoxWithConstraintsScope.UI(
    state: AccountsState,
    categoriesState: CategoriesScreenState,
    onEvent: (AccountsEvent) -> Unit = {},
    onCategoriesEvent: (CategoriesScreenEvent) -> Unit = {},
    onOpenHomeTab: () -> Unit,
    onAddAccount: () -> Unit = {},
) {
    val nav = navigation()
    val datePicker = LocalDatePicker.current
    val listState = rememberScrollPositionListState(
        key = "accounts_lazy_column"
    )

    val accountList = remember { mutableStateListOf<AccountData>() }
    val expandedAccounts = remember { mutableStateMapOf<UUID, Boolean>() }
    var categoryModalVisible by remember { mutableStateOf(false) }
    var periodModal: TimePeriod? by remember { mutableStateOf(null) }

    val dragDropState = rememberDragDropState(
        lazyListState = listState,
        draggable = { index -> index in 1..accountList.size },
        onMove = { fromIndex, toIndex ->
            val from = fromIndex - HEADER_ITEMS_COUNT
            val to = toIndex - HEADER_ITEMS_COUNT
            if (from in accountList.indices && to in accountList.indices) {
                accountList.add(to, accountList.removeAt(from))
            }
        },
    )

    LaunchedEffect(state.accountsData) {
        if (dragDropState.draggingItemIndex != null) return@LaunchedEffect
        val incoming = state.accountsData
        val sameOrder = incoming.size == accountList.size &&
            incoming.indices.all { incoming[it].account.id == accountList[it].account.id }
        if (sameOrder) {
            incoming.forEachIndexed { i, data -> accountList[i] = data }
        } else {
            accountList.clear()
            accountList.addAll(incoming)
        }
    }

    // Categories grouped under the account that owns them, plus a trailing "unassigned" bucket.
    val categoriesByAccount = remember(categoriesState.categories, categoriesState.accounts) {
        categoriesState.accounts.associate { header ->
            header.id to categoriesState.categories.filter {
                it.category.id.value in header.categoryIds
            }
        }
    }
    val unassignedCategories = remember(categoriesState.categories, categoriesState.accounts) {
        val assigned = categoriesState.accounts.flatMapTo(HashSet()) { it.categoryIds }
        categoriesState.categories.filter { it.category.id.value !in assigned }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .horizontalSwipeListener(
                sensitivity = 200,
                state = rememberSwipeListenerState(),
                onSwipeLeft = { onOpenHomeTab() },
                onSwipeRight = { onOpenHomeTab() }
            ),
        state = listState
    ) {
        item {
            Spacer(Modifier.height(20.dp))

            AccountsHeaderRow(
                onSort = { onCategoriesEvent(CategoriesScreenEvent.OnSortOrderModalVisible(true)) }
            )

            Spacer(Modifier.height(16.dp))

            NetWorthCard(
                currency = state.baseCurrency,
                netWorth = state.netWorth,
                change = state.netWorthChange,
                hideBalance = state.hideTotalBalance,
            )

            Spacer(Modifier.height(16.dp))

            MonthSelectorBar(
                period = categoriesState.period,
                onPrevious = { onCategoriesEvent(CategoriesScreenEvent.OnPreviousMonth) },
                onNext = { onCategoriesEvent(CategoriesScreenEvent.OnNextMonth) },
                onClick = { periodModal = categoriesState.period }
            )

            Spacer(Modifier.height(4.dp))
        }

        itemsIndexed(
            items = accountList,
            key = { _, item -> item.account.id.value },
        ) { index, accountData ->
            val accountId = accountData.account.id.value
            DraggableItem(
                dragDropState = dragDropState,
                index = index + HEADER_ITEMS_COUNT,
                key = accountId,
                onDragFinished = {
                    onEvent(AccountsEvent.OnReorder(accountIds = accountList.map { it.account.id }))
                },
            ) { isDragging ->
                Spacer(Modifier.height(10.dp))
                ExpandableAccountCard(
                    accountData = accountData,
                    categories = categoriesByAccount[accountId] ?: emptyList(),
                    currency = categoriesState.baseCurrency,
                    expanded = expandedAccounts[accountId] ?: true,
                    isDragging = isDragging,
                    onToggleExpand = {
                        expandedAccounts[accountId] = !(expandedAccounts[accountId] ?: true)
                    },
                    onOpenAccount = {
                        nav.navigateTo(
                            TransactionsScreen(accountId = accountId, categoryId = null)
                        )
                    },
                    onOpenCategory = { categoryData ->
                        nav.navigateTo(
                            TransactionsScreen(
                                accountId = null,
                                categoryId = categoryData.category.id.value
                            )
                        )
                    },
                )
            }
        }

        item {
            if (unassignedCategories.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                UnassignedHeaderRow(count = unassignedCategories.size)
                Spacer(Modifier.height(8.dp))
                CategoriesCard(
                    categories = unassignedCategories,
                    currency = categoriesState.baseCurrency,
                    onCategoryClick = { categoryData ->
                        nav.navigateTo(
                            TransactionsScreen(
                                accountId = null,
                                categoryId = categoryData.category.id.value
                            )
                        )
                    },
                )
            }

            Spacer(Modifier.height(16.dp))
            DashedAddCategoryButton(onClick = { categoryModalVisible = true })
            Spacer(Modifier.height(12.dp))
            DashedAddAccountButton(onClick = onAddAccount)
            Spacer(Modifier.height(150.dp)) // scroll hack
        }
    }

    CreateCategoryModal(
        visible = categoryModalVisible,
        usedColors = categoriesState.categories.map { it.category.color.value },
        onCreateCategory = { onCategoriesEvent(CategoriesScreenEvent.OnCreateCategory(it)) },
        dismiss = { categoryModalVisible = false }
    )

    SortModal(
        initialType = categoriesState.sortOrder,
        items = categoriesState.sortOrderItems,
        visible = categoriesState.sortModalVisible,
        dismiss = { onCategoriesEvent(CategoriesScreenEvent.OnSortOrderModalVisible(false)) },
        onSortOrderChange = {
            onCategoriesEvent(CategoriesScreenEvent.OnReorder(categoriesState.categories, it))
        }
    )

    ChoosePeriodModal(
        modal = periodModal,
        dismiss = { periodModal = null },
        saveSelectedPeriod = {}, // keep independent from the global/home period
        pickDate = { minDate, maxDate, initialDate, onDatePicked ->
            datePicker.pickDate(
                minDate = minDate,
                maxDate = maxDate,
                initialDate = initialDate,
                onDatePicked = onDatePicked
            )
        },
        onPeriodSelected = { onCategoriesEvent(CategoriesScreenEvent.OnSelectPeriod(it)) }
    )
}
@Composable
private fun AccountsHeaderRow(onSort: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.accounts),
            style = AccountsTheme.typo.b1.copy(
                color = AccountsTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start,
                fontSize = 24.sp,
            ),
        )

        Spacer(Modifier.weight(1f))

        FilledIconButton(
            icon = R.drawable.ic_sort_by_alpha_24,
            backgroundColor = AccountsTheme.colors.medium,
            tint = AccountsTheme.colors.pureInverse,
            onClick = onSort,
            clickAreaPadding = 12.dp
        )
    }
}

@Composable
private fun NetWorthCard(
    currency: String,
    netWorth: Double,
    change: Double,
    hideBalance: Boolean,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(AccountsTheme.shapes.r4)
            .background(IvyGradients.Dark.asHorizontalBrush())
            .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.net_worth),
            style = AccountsTheme.typo.c.copy(
                color = White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
            ),
        )

        Spacer(Modifier.height(10.dp))

        BalanceRow(
            currency = currency,
            balance = netWorth,
            textColor = White,
            hiddenMode = hideBalance,
            balanceFontSize = 32.sp,
            shortenBigNumbers = true,
        )

        if (!hideBalance && change.absoluteValue >= 0.005) {
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.vs_last_month),
                    style = AccountsTheme.typo.c.copy(
                        color = White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start,
                    ),
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "${if (change >= 0) "▲" else "▼"} ${change.absoluteValue.format(currency)}",
                    style = AccountsTheme.typo.c.copy(
                        color = if (change >= 0) Green else Red,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                    ),
                )
            }
        }
    }
}

@Composable
private fun MonthSelectorBar(
    period: TimePeriod,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
) {
    val periodState = LocalPeriodState.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
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
            shape = AccountsTheme.shapes.rFull,
            backgroundColor = AccountsTheme.colors.pure,
            iconTint = AccountsTheme.colors.pureInverse,
            borderColor = AccountsTheme.colors.medium,
            textStyle = AccountsTheme.typo.b2.copy(
                fontWeight = FontWeight.Bold,
                color = AccountsTheme.colors.pureInverse,
                textAlign = TextAlign.Start
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
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit
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
            tint = AccountsTheme.colors.pureInverse,
            contentDescription = contentDescription
        )
    }
}
@Composable
private fun ExpandableAccountCard(
    accountData: AccountData,
    categories: List<CategoryData>,
    currency: String,
    expanded: Boolean,
    isDragging: Boolean,
    onToggleExpand: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenCategory: (CategoryData) -> Unit,
) {
    val account = accountData.account
    val accountColor = account.color.value.toComposeColor()
    val accountCurrency = account.asset.code
    val secondaryColor = AccountsTheme.colors.pureInverse.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .thenIf(isDragging) {
                shadow(elevation = 12.dp, shape = AccountsTheme.shapes.r4)
            }
            .clip(AccountsTheme.shapes.r4)
            .background(AccountsTheme.colors.pure)
            .border(
                width = if (isDragging) 1.5.dp else 1.dp,
                color = if (isDragging) accountColor else AccountsTheme.colors.medium,
                shape = AccountsTheme.shapes.r4,
            )
            .drawBehind {
                // Colour stripe down the left edge, tracking the card's live (animating) height
                // so it stays continuous while the categories expand/collapse.
                drawRect(
                    color = accountColor,
                    size = Size(width = 5.dp.toPx(), height = size.height),
                )
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenAccount)
                .padding(start = 17.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accountColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                ItemIconSDefaultIcon(
                    iconName = account.icon?.id,
                    defaultIcon = R.drawable.ic_custom_account_s,
                    tint = accountColor
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name.value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = AccountsTheme.typo.b2.copy(
                        color = AccountsTheme.colors.pureInverse,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${categories.size} 个类别",
                    maxLines = 1,
                    style = AccountsTheme.typo.c.copy(
                        color = secondaryColor,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start
                    )
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = if (shouldShortAmount(accountData.balance)) {
                    shortenAmount(accountData.balance)
                } else {
                    accountData.balance.format(accountCurrency)
                },
                maxLines = 1,
                style = AccountsTheme.typo.b1.copy(
                    color = AccountsTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.End,
                    fontSize = 18.sp,
                ),
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onToggleExpand)
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                ResourceIcon(
                    modifier = Modifier.rotate(if (expanded) 180f else 0f),
                    icon = R.drawable.ic_expandarrow,
                    tint = secondaryColor,
                    contentDescription = "toggle",
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (categories.isEmpty()) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 17.dp, end = 16.dp, bottom = 14.dp),
                        text = "记账时选择的类别会自动归入该账户",
                        style = AccountsTheme.typo.c.copy(
                            color = secondaryColor,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        )
                    )
                } else {
                    categories.forEach { categoryData ->
                        RowDivider()
                        CategoryRow(
                            categoryData = categoryData,
                            currency = currency,
                            onClick = { onOpenCategory(categoryData) },
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(AccountsTheme.colors.medium)
    )
}

@Composable
private fun CategoryRow(
    categoryData: CategoryData,
    currency: String,
    onClick: () -> Unit,
) {
    val category = categoryData.category
    val categoryColor = category.color.value.toComposeColor()
    val secondaryColor = AccountsTheme.colors.pureInverse.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(categoryColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            ItemIconSDefaultIcon(
                iconName = category.icon?.id,
                defaultIcon = R.drawable.ic_custom_category_s,
                tint = categoryColor
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name.value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AccountsTheme.typo.b2.copy(
                    color = AccountsTheme.colors.pureInverse,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = categoryMonthlySubtitle(categoryData, currency),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AccountsTheme.typo.c.copy(
                    color = secondaryColor,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start
                )
            )
        }

        Spacer(Modifier.width(8.dp))

        ResourceIcon(
            icon = R.drawable.ic_arrow_right,
            tint = AccountsTheme.colors.pureInverse.copy(alpha = 0.3f),
        )
    }
}

private fun categoryMonthlySubtitle(categoryData: CategoryData, currency: String): String {
    val count = categoryData.monthlyCount
    if (count == 0) return "本月无记录"

    val net = categoryData.monthlyBalance
    val prefix = when {
        net > 0.0 -> "+"
        net < 0.0 -> "-"
        else -> ""
    }
    val absNet = abs(net)
    val amountStr = if (shouldShortAmount(absNet)) {
        shortenAmount(absNet)
    } else {
        absNet.format(currency)
    }
    return "本月 $prefix$amountStr · $count 笔"
}
@Composable
private fun UnassignedHeaderRow(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(AccountsTheme.colors.pureInverse.copy(alpha = 0.4f))
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = "未分配",
            style = AccountsTheme.typo.b2.copy(
                color = AccountsTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = "$count",
            style = AccountsTheme.typo.c.copy(
                color = AccountsTheme.colors.pureInverse.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        )
    }
}

@Composable
private fun CategoriesCard(
    categories: List<CategoryData>,
    currency: String,
    onCategoryClick: (CategoryData) -> Unit,
) {
    if (categories.isEmpty()) return
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(AccountsTheme.shapes.r4)
            .background(AccountsTheme.colors.pure)
            .border(1.dp, AccountsTheme.colors.medium, AccountsTheme.shapes.r4),
    ) {
        categories.forEachIndexed { index, categoryData ->
            if (index > 0) RowDivider()
            CategoryRow(
                categoryData = categoryData,
                currency = currency,
                onClick = { onCategoryClick(categoryData) },
            )
        }
    }
}

@Composable
private fun DashedAddCategoryButton(onClick: () -> Unit) {
    val borderColor = AccountsTheme.colors.medium
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(AccountsTheme.shapes.r4)
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                )
            }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ResourceIcon(
            icon = R.drawable.ic_plus,
            tint = AccountsTheme.colors.pureInverse,
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.add_category),
            style = AccountsTheme.typo.b2.copy(
                color = AccountsTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }
}

@Composable
private fun DashedAddAccountButton(onClick: () -> Unit) {
    val borderColor = AccountsTheme.colors.medium
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(AccountsTheme.shapes.r4)
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                )
            }
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ResourceIcon(
            icon = R.drawable.ic_plus,
            tint = AccountsTheme.colors.pureInverse,
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.add_account),
            style = AccountsTheme.typo.b2.copy(
                color = AccountsTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }
}

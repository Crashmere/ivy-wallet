package com.ivy.bulkedit

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.widget.Toast
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
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getFromValue
import com.ivy.ui.R
import com.ivy.ui.compose.FilledIconButton
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
import com.ivy.ui.theme.colors.IvyFixedColors
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

@Composable
fun BoxWithConstraintsScope.BulkEditScreen() {
    val viewModel: BulkEditViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    UI(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    uiState: BulkEditState,
    onEvent: (BulkEditEvent) -> Unit,
) {
    val nav = navigation()
    val datePicker = LocalDatePicker.current
    val listState = rememberLazyListState()

    var periodModal: TimePeriod? by remember { mutableStateOf(null) }
    var bulkChangeModalVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        state = listState
    ) {
        item {
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(24.dp))

                MonthSelector(
                    period = uiState.period,
                    onPrevious = { onEvent(BulkEditEvent.OnPreviousMonth) },
                    onNext = { onEvent(BulkEditEvent.OnNextMonth) },
                    onClick = { periodModal = uiState.period }
                )

                Spacer(Modifier.weight(1f))

                Spacer(Modifier.width(24.dp))
            }

            Spacer(Modifier.height(20.dp))

            FilterSection(
                uiState = uiState,
                onEvent = onEvent
            )

            Spacer(Modifier.height(20.dp))

            SummaryStrip(
                count = uiState.matchingCount,
                income = uiState.income,
                expenses = uiState.expenses,
                baseCurrency = uiState.baseCurrency
            )

            Spacer(Modifier.height(16.dp))
        }

        transactions(
            baseData = TransactionListData(
                baseCurrency = uiState.baseCurrency,
                accounts = uiState.allAccounts.map { it.toTransactionListAccount() },
                categories = uiState.allCategories.map { it.toTransactionListCategory() }
            ),
            upcoming = null,
            setUpcomingExpanded = { },
            overdue = null,
            setOverdueExpanded = { },
            history = uiState.matchingTransactions.map { it.toTransactionListHistoryItem() },
            onPayOrGet = { },
            onTransactionClick = { transactionId, transactionType ->
                nav.navigateTo(
                    EditTransactionScreen(
                        initialTransactionId = transactionId,
                        type = transactionType.toRouteType()
                    )
                )
            },
            onAccountClick = { },
            onCategoryClick = { },
            emptyStateTitle = "无匹配交易",
            emptyStateText = "调整上方的时间范围或筛选条件后，匹配的交易会显示在这里。",
            dateDividerMarginTop = 16.dp,
            shouldShowAccountSpecificColorInTransactions =
            uiState.shouldShowAccountColorsInTransactions
        )

        item {
            Spacer(Modifier.height(160.dp))
        }
    }

    BulkEditBottomBar(
        count = uiState.matchingCount,
        onClose = { nav.back() },
        onBulkEdit = { bulkChangeModalVisible = true }
    )

    BulkChangeModal(
        visible = bulkChangeModalVisible,
        state = uiState,
        onEvent = onEvent,
        dismiss = { bulkChangeModalVisible = false }
    )

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
        onPeriodSelected = { onEvent(BulkEditEvent.OnSelectPeriod(it)) }
    )
}

@Composable
private fun MonthSelector(
    period: TimePeriod,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
            shape = BulkEditTheme.shapes.rFull,
            backgroundColor = BulkEditTheme.colors.pure,
            iconTint = BulkEditTheme.colors.pureInverse,
            borderColor = BulkEditTheme.colors.medium,
            textStyle = BulkEditTheme.typo.b2.copy(
                fontWeight = FontWeight.Bold,
                color = BulkEditTheme.colors.pureInverse,
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
    @DrawableRes icon: Int,
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
            tint = BulkEditTheme.colors.pureInverse,
            contentDescription = contentDescription
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    uiState: BulkEditState,
    onEvent: (BulkEditEvent) -> Unit,
) {
    val hasCategoryOptions = uiState.filterCategories.isNotEmpty() ||
            uiState.filterHasUncategorized
    val hasTagOptions = uiState.filterTags.isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "筛选条件",
                style = BulkEditTheme.typo.b2.copy(
                    color = BulkEditTheme.colors.pureInverse,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
            )

            Spacer(Modifier.weight(1f))

            if (uiState.isFilterActive) {
                Text(
                    modifier = Modifier
                        .clip(BulkEditTheme.shapes.rFull)
                        .clickable { onEvent(BulkEditEvent.ClearFilters) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    text = "清除",
                    style = BulkEditTheme.typo.c.copy(
                        color = BulkEditTheme.colors.gray,
                        textAlign = TextAlign.Start
                    )
                )
            }
        }

        if (!hasCategoryOptions && !hasTagOptions) {
            Spacer(Modifier.height(12.dp))
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = "该时间范围内暂无可筛选的类别或标签。",
                style = BulkEditTheme.typo.c.copy(
                    color = BulkEditTheme.colors.gray,
                    textAlign = TextAlign.Start
                )
            )
            return@Column
        }

        if (hasCategoryOptions) {
            Spacer(Modifier.height(12.dp))
            SubLabel("类别")
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.filterCategories.forEach { category ->
                    FilterChip(
                        text = category.name.value,
                        selected = category.id.value in uiState.selectedCategoryIds,
                        onClick = {
                            onEvent(BulkEditEvent.ToggleCategoryFilter(category.id.value))
                        }
                    )
                }
                if (uiState.filterHasUncategorized) {
                    FilterChip(
                        text = "无类别",
                        selected = uiState.uncategorizedSelected,
                        onClick = { onEvent(BulkEditEvent.ToggleUncategorizedFilter) }
                    )
                }
            }
        }

        if (hasTagOptions) {
            Spacer(Modifier.height(12.dp))
            SubLabel("标签")
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.filterTags.forEach { tag ->
                    FilterChip(
                        text = "#${tag.name.value}",
                        selected = tag.id.value in uiState.selectedTagIds,
                        onClick = { onEvent(BulkEditEvent.ToggleTagFilter(tag.id.value)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubLabel(text: String) {
    Text(
        modifier = Modifier.padding(horizontal = 24.dp),
        text = text,
        style = BulkEditTheme.typo.c.copy(
            color = BulkEditTheme.colors.gray,
            textAlign = TextAlign.Start
        )
    )
}

@Composable
internal fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                color = if (selected) {
                    BulkEditTheme.colors.pureInverse
                } else {
                    Color.Transparent
                },
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = if (selected) {
                    BulkEditTheme.colors.pureInverse
                } else {
                    BulkEditTheme.colors.medium
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = text,
        style = BulkEditTheme.typo.c.copy(
            color = if (selected) {
                BulkEditTheme.colors.pure
            } else {
                BulkEditTheme.colors.pureInverse
            },
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
        )
    )
}

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
            .border(1.dp, BulkEditTheme.colors.gray, BulkEditTheme.shapes.r4)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "匹配交易",
                style = BulkEditTheme.typo.c.copy(color = BulkEditTheme.colors.gray)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "$count 笔",
                style = BulkEditTheme.typo.nB1.copy(
                    color = BulkEditTheme.colors.pureInverse,
                    fontWeight = FontWeight.Bold
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
                amountColor = IvyFixedColors.Green
            )

            Spacer(Modifier.width(12.dp))

            SummaryAmount(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                label = "支出",
                amount = expenses,
                currency = baseCurrency,
                amountColor = BulkEditTheme.colors.pureInverse
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
            style = BulkEditTheme.typo.c.copy(color = BulkEditTheme.colors.gray)
        )
        Spacer(Modifier.height(4.dp))
        AmountCurrencyB1(
            amount = amount,
            currency = currency,
            textColor = amountColor
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.BulkEditBottomBar(
    count: Int,
    onClose: () -> Unit,
    onBulkEdit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(BulkEditTheme.colors.pure)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledIconButton(
            icon = R.drawable.ic_back,
            backgroundColor = BulkEditTheme.colors.medium,
            tint = BulkEditTheme.colors.pureInverse,
            onClick = onClose,
            clickAreaPadding = 12.dp
        )

        Spacer(Modifier.weight(1f))

        val enabled = count > 0
        Row(
            modifier = Modifier
                .clip(BulkEditTheme.shapes.rFull)
                .background(
                    if (enabled) {
                        BulkEditTheme.colors.pureInverse
                    } else {
                        BulkEditTheme.colors.medium
                    }
                )
                .clickable(enabled = enabled, onClick = onBulkEdit)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ResourceIcon(
                icon = R.drawable.ic_edit,
                tint = if (enabled) BulkEditTheme.colors.pure else BulkEditTheme.colors.gray
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "批量修改 ($count)",
                style = BulkEditTheme.typo.b2.copy(
                    color = if (enabled) BulkEditTheme.colors.pure else BulkEditTheme.colors.gray,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
            )
        }
    }
}

private fun TransactionListTransactionType.toRouteType(): TransactionRouteType {
    return TransactionRouteType.valueOf(name)
}

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

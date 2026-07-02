package com.ivy.piechart

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.model.TransactionType
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.ui.compose.thenIf
import com.ivy.ui.period.LocalPeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.period.displayShort
import com.ivy.ui.compose.CloseIconButton
import com.ivy.ui.compose.drawColoredShadow
import com.ivy.data.model.currency.format
import com.ivy.ui.compose.horizontalSwipeListener
import com.ivy.ui.compose.rememberSwipeListenerState
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.PieChartStatisticScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.ui.money.BalanceRow
import com.ivy.ui.icon.ItemIconMDefaultIcon
import com.ivy.ui.compose.OutlinedPillButton
import com.ivy.ui.theme.colors.findContrastTextColor
import com.ivy.ui.modal.ChoosePeriodModal
import com.ivy.ui.theme.colors.toComposeColor
import com.ivy.ui.money.AmountCurrencyB1
import kotlinx.collections.immutable.toImmutableList

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.PieChartStatisticScreen(
    screen: PieChartStatisticScreen
) {
    val viewModel: PieChartStatisticViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    LaunchedEffect(Unit) {
        viewModel.start(
            type = screen.type.toTransactionType(),
            accountIdFilterList = screen.accountIdFilterList,
            filterExcluded = screen.filterExcluded,
            inputTransactionIds = screen.transactionIds,
            transfersAsIncomeExpense = screen.treatTransfersAsIncomeExpense
        )
    }

    UI(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    state: PieChartStatisticState,
    onEvent: (PieChartStatisticEvent) -> Unit = {}
) {
    val nav = navigation()
    val periodState = LocalPeriodState.current
    val datePicker = LocalDatePicker.current
    val lazyState = rememberScrollPositionListState(
        key = "item_pie_chart_lazy_column"
    )
    val expanded = lazyState.firstVisibleItemIndex < 1
    val percentExpanded by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = com.ivy.ui.animation.springBounce(),
        label = "percent expanded"
    )
    var choosePeriodModal: TimePeriod? by remember { mutableStateOf(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        state = lazyState
    ) {
        stickyHeader {
            Header(
                transactionType = state.transactionType,
                period = state.period,
                startDateOfMonth = periodState.startDayOfMonth,
                percentExpanded = percentExpanded,
                currency = state.baseCurrency,
                amount = state.totalAmount,
                onShowMonthModal = {
                    choosePeriodModal = state.period
                },
                onSelectNextMonth = {
                    onEvent(PieChartStatisticEvent.OnSelectNextMonth)
                },
                onSelectPreviousMonth = {
                    onEvent(PieChartStatisticEvent.OnSelectPreviousMonth)
                },
                showCloseButtonOnly = state.showCloseButtonOnly,
                onClose = {
                    nav.back()
                },
                onAdd = { transactionType ->
                    nav.navigateTo(
                        EditTransactionScreen(
                            initialTransactionId = null,
                            type = transactionType.toRouteType()
                        )
                    )
                }
            )
        }

        item {
            Spacer(Modifier.height(20.dp))

            Text(
                modifier = Modifier
                    .padding(start = 32.dp)
                    .testTag("piechart_title"),
                text = if (state.transactionType == TransactionType.EXPENSE) {
                    stringResource(R.string.expenses)
                } else {
                    stringResource(R.string.income)
                },
                style = PieChartTheme.typo.b1.copy(
                    color = PieChartTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                )
            )

            BalanceRow(
                modifier = Modifier
                    .padding(start = 32.dp, end = 16.dp)
                    .testTag("piechart_total_amount")
                    .alpha(percentExpanded),
                currency = state.baseCurrency,
                balance = state.totalAmount,
                currencyUpfront = false,
                currencyFontSize = 30.sp
            )
        }

        item {
            Spacer(Modifier.height(40.dp))

            PieChart(
                type = state.transactionType,
                categoryAmounts = state.categoryAmounts,
                selectedCategory = state.selectedCategory,
                onCategoryClick = { clickedCategory ->
                    onEvent(PieChartStatisticEvent.OnCategoryClicked(clickedCategory?.id?.value))
                }
            )

            Spacer(Modifier.height(48.dp))
        }

        itemsIndexed(
            items = state.categoryAmounts
        ) { index, item ->
            if (item.amount != 0.0) {
                if (index != 0) {
                    Spacer(Modifier.height(16.dp))
                }

                CategoryAmountCard(
                    categoryAmount = item,
                    currency = state.baseCurrency,
                    totalAmount = state.totalAmount,
                    selectedCategory = state.selectedCategory
                ) {
                    nav.navigateTo(
                        TransactionsScreen(
                            categoryId = item.category?.id?.value,
                            unspecifiedCategory = item.isCategoryUnspecified,
                            accountIdFilterList = state.accountIdFilterList,
                            transactionIds = item.associatedTransactions.map { it.id }
                                .toImmutableList(),
                            containsTransferTransactions = item.associatedTransactions.any {
                                it.type == TransactionType.TRANSFER
                            }
                        )
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(160.dp)) // scroll hack
        }
    }

    ChoosePeriodModal(
        modal = choosePeriodModal,
        dismiss = {
            choosePeriodModal = null
        },
        saveSelectedPeriod = {},
        pickDate = { minDate, maxDate, initialDate, onDatePicked ->
            datePicker.pickDate(
                minDate = minDate,
                maxDate = maxDate,
                initialDate = initialDate,
                onDatePicked = onDatePicked
            )
        },
    ) {
        onEvent(PieChartStatisticEvent.OnSetPeriod(it))
    }
}

private fun TransactionType.toRouteType(): TransactionRouteType {
    return TransactionRouteType.valueOf(name)
}

private fun TransactionRouteType.toTransactionType(): TransactionType {
    return TransactionType.valueOf(name)
}

@Composable
private fun Header(
    transactionType: TransactionType,
    period: TimePeriod,
    startDateOfMonth: Int,
    percentExpanded: Float,

    currency: String,
    amount: Double,

    onShowMonthModal: () -> Unit,
    onSelectNextMonth: () -> Unit,
    onSelectPreviousMonth: () -> Unit,

    onClose: () -> Unit,
    onAdd: (TransactionType) -> Unit,
    showCloseButtonOnly: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PieChartTheme.colors.pure.copy(alpha = 0.95f))
            .statusBarsPadding()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(20.dp))

        CloseButton {
            onClose()
        }

        // Balance mini row
        if (percentExpanded < 1f) {
            Spacer(Modifier.width(12.dp))

            BalanceRow(
                modifier = Modifier
                    .alpha(1f - percentExpanded),
                currency = currency,
                balance = amount,
                spacerCurrency = 8.dp,
                currencyFontSize = 20.sp,
                balanceFontSize = 22.sp,
            )
        }

        if (!showCloseButtonOnly) {
            Spacer(Modifier.weight(1f))

            OutlinedPillButton(
                modifier = Modifier.horizontalSwipeListener(
                    sensitivity = 75,
                    state = rememberSwipeListenerState(),
                    onSwipeLeft = {
                        onSelectNextMonth()
                    },
                    onSwipeRight = {
                        onSelectPreviousMonth()
                    }
                ),
                iconStart = R.drawable.ic_calendar,
                text = period.displayShort(startDateOfMonth),
                shape = PieChartTheme.shapes.rFull,
                backgroundColor = PieChartTheme.colors.pure,
                iconTint = PieChartTheme.colors.pureInverse,
                borderColor = PieChartTheme.colors.medium,
                textStyle = PieChartTheme.typo.b2.copy(
                    fontWeight = FontWeight.Bold,
                    color = PieChartTheme.colors.pureInverse,
                    textAlign = TextAlign.Start,
                ),
            ) {
                onShowMonthModal()
            }

            if (percentExpanded > 0f) {
                Spacer(Modifier.width(12.dp))

                val backgroundGradient = if (transactionType == TransactionType.EXPENSE) {
                    Gradient(PieChartTheme.colors.pureInverse, PieChartTheme.colors.gray)
                } else {
                    IvyGradients.Green
                }
                PieChartCircleButtonFilledGradient(
                    modifier = Modifier
                        .thenIf(percentExpanded == 1f) {
                            drawColoredShadow(backgroundGradient.startColor)
                        }
                        .alpha(percentExpanded)
                        .size(com.ivy.ui.animation.lerp(1, 40, percentExpanded).dp),
                    iconPadding = 4.dp,
                    icon = R.drawable.ic_plus,
                    backgroundGradient = backgroundGradient,
                    tint = if (transactionType == TransactionType.EXPENSE) {
                        PieChartTheme.colors.pure
                    } else {
                        White
                    }
                ) {
                    onAdd(transactionType)
                }
            }

            Spacer(Modifier.width(20.dp))
        }
    }
}

@Composable
private fun PieChartCircleButtonFilledGradient(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    contentDescription: String = "icon",
    iconPadding: Dp = 8.dp,
    backgroundGradient: Gradient = Gradient.solid(PieChartTheme.colors.medium),
    tint: Color? = PieChartTheme.colors.pureInverse,
    onClick: () -> Unit,
) {
    Icon(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundGradient.asHorizontalBrush(), CircleShape)
            .clickable(onClick = onClick)
            .padding(iconPadding),
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        tint = tint ?: Color.Unspecified,
    )
}

@Composable
private fun CloseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CloseIconButton(
        modifier = modifier,
        backgroundColor = PieChartTheme.colors.pure,
        borderColor = PieChartTheme.colors.medium,
        tint = PieChartTheme.colors.pureInverse,
        onClick = onClick,
    )
}

@Composable
private fun CategoryAmountCard(
    categoryAmount: CategoryAmount,
    currency: String,
    totalAmount: Double,

    selectedCategory: SelectedCategory?,

    onClick: () -> Unit
) {
    val category = categoryAmount.category
    val amount = categoryAmount.amount

    val categoryColor = category?.color?.value?.toComposeColor()
        ?: PieChartTheme.colors.gray
    val selectedState = when {
        selectedCategory == null -> {
            // no selectedCategory
            false
        }

        categoryAmount.category?.id?.value == selectedCategory.categoryId -> {
            // selectedCategory && we're selected
            true
        }

        else -> false
    }
    val backgroundColor = if (selectedState) categoryColor else PieChartTheme.colors.medium

    val textColor = findContrastTextColor(
        backgroundColor = backgroundColor
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .thenIf(selectedState) {
                drawColoredShadow(backgroundColor)
            }
            .clip(PieChartTheme.shapes.r3)
            .background(backgroundColor, PieChartTheme.shapes.r3)
            .clickable {
                onClick()
            }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(20.dp))

        ItemIconMDefaultIcon(
            modifier = Modifier.background(categoryColor, CircleShape),
            iconName = category?.icon?.id,
            defaultIcon = R.drawable.ic_custom_category_m,
            tint = findContrastTextColor(categoryColor),
            iconContentScale = ContentScale.None,
        )

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    text = category?.name?.value ?: stringResource(R.string.unspecified),
                    style = PieChartTheme.typo.b2.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                )

                PercentText(
                    amount = amount,
                    totalAmount = totalAmount,
                    selectedState = selectedState,
                    contrastColor = textColor
                )

                Spacer(Modifier.width(24.dp))
            }

            Spacer(Modifier.height(4.dp))

            Row {
                AmountCurrencyB1(
                    amount = amount,
                    currency = currency,
                    textColor = textColor,
                    amountFontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun PercentText(
    amount: Double,
    totalAmount: Double,
    selectedState: Boolean,
    contrastColor: Color
) {
    Text(
        text = if (totalAmount != 0.0) {
            stringResource(R.string.percent, ((amount / totalAmount) * 100).format(2))
        } else {
            stringResource(R.string.percent, "0")
        },
        style = PieChartTheme.typo.nB2.copy(
            color = if (selectedState) contrastColor else PieChartTheme.colors.pureInverse,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Start
        )
    )
}

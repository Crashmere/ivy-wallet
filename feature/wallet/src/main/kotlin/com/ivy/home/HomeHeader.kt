package com.ivy.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.ui.period.LocalPeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.period.displayShort
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.drawColoredShadow
import com.ivy.data.model.currency.format
import com.ivy.data.model.currency.shortenAmount
import com.ivy.data.model.currency.shouldShortAmount
import com.ivy.ui.compose.horizontalSwipeListener
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.compose.rememberSwipeListenerState
import com.ivy.ui.animation.springBounce
import com.ivy.ui.R
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.ui.money.BalanceRow
import com.ivy.ui.compose.OutlinedPillButton
import com.ivy.ui.compose.ResourceIcon
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.absoluteValue

@ExperimentalAnimationApi
@Composable
internal fun HomeHeader(
    expanded: Boolean,
    period: TimePeriod,
    onShowMonthModal: () -> Unit,
    onSelectNextMonth: () -> Unit,
    onSelectPreviousMonth: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenBulkEdit: () -> Unit,
) {
    Column {
        val percentExpanded by animateFloatAsState(
            targetValue = if (expanded) 1f else 0f,
            animationSpec = springBounce(
                stiffness = Spring.StiffnessLow
            ),
            label = "Home Header Expand Collapse"
        )

        Spacer(Modifier.height(16.dp))

        HeaderStickyRow(
            period = period,
            onShowMonthModal = onShowMonthModal,
            onSelectNextMonth = onSelectNextMonth,
            onSelectPreviousMonth = onSelectPreviousMonth,
            onOpenSearch = onOpenSearch,
            onOpenBulkEdit = onOpenBulkEdit,
        )

        Spacer(Modifier.height(14.dp))

        if (percentExpanded < 0.5f) {
            HomeTransactionsDividerLine(
                modifier = Modifier.alpha(1f - percentExpanded),
                paddingHorizontal = 0.dp
            )
        }
    }
}

@Composable
private fun HeaderStickyRow(
    period: TimePeriod,
    onShowMonthModal: () -> Unit,
    onSelectNextMonth: () -> Unit,
    onSelectPreviousMonth: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenBulkEdit: () -> Unit,
) {
    val periodState = LocalPeriodState.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonthArrowButton(
            icon = R.drawable.ic_back,
            contentDescription = "Previous month",
            onClick = onSelectPreviousMonth,
        )

        OutlinedPillButton(
            modifier = Modifier.horizontalSwipeListener(
                sensitivity = 75,
                state = rememberSwipeListenerState(),
                onSwipeLeft = {
                    onSelectNextMonth()
                },
                onSwipeRight = {
                    onSelectPreviousMonth()
                },
            ),
            iconStart = R.drawable.ic_calendar,
            text = period.displayShort(periodState.startDayOfMonth),
            shape = HomeTheme.shapes.rFull,
            backgroundColor = HomeTheme.colors.pure,
            minWidth = 108.dp,
            iconTint = HomeTheme.colors.pureInverse,
            borderColor = HomeTheme.colors.medium,
            textStyle = HomeTheme.typo.b2.copy(
                fontWeight = FontWeight.Bold,
                color = HomeTheme.colors.pureInverse,
                textAlign = TextAlign.Start,
            ),
        ) {
            onShowMonthModal()
        }

        MonthArrowButton(
            icon = R.drawable.ic_arrow_right,
            contentDescription = "Next month",
            onClick = onSelectNextMonth,
        )

        Spacer(Modifier.weight(1f))

        HeaderActionIcon(
            icon = R.drawable.ic_search,
            contentDescription = stringResource(R.string.search_transactions),
            onClick = onOpenSearch,
        )

        Spacer(Modifier.width(10.dp))

        HeaderActionIcon(
            icon = R.drawable.home_more_menu_bulk_edit,
            contentDescription = "批量修改",
            onClick = onOpenBulkEdit,
        )
    }
}

@Composable
private fun MonthArrowButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        ResourceIcon(
            icon = icon,
            tint = HomeTheme.colors.pureInverse,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun HeaderActionIcon(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(HomeTheme.colors.pure)
            .border(1.dp, HomeTheme.colors.medium, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        ResourceIcon(
            icon = icon,
            tint = HomeTheme.colors.pureInverse,
            contentDescription = contentDescription,
        )
    }
}

@ExperimentalAnimationApi
@Composable
internal fun CashFlowInfo(
    currency: String,
    balance: Double,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    hideBalance: Boolean,
    hideIncome: Boolean,
    onHiddenIncomeClick: () -> Unit,
    onBalanceClick: () -> Unit,
    onHiddenBalanceClick: () -> Unit,
    onOpenIncomePieChart: () -> Unit,
    onOpenExpensePieChart: () -> Unit,
    trend: ImmutableList<Float>,
    modifier: Modifier = Modifier
) {
    val net = monthlyIncome - monthlyExpenses

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .drawColoredShadow(IvyGradients.Mint.startColor)
            .clip(HomeTheme.shapes.r4)
            .background(IvyGradients.Mint.asHorizontalBrush())
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.month_net),
                style = HomeTheme.typo.c.copy(
                    color = White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                ),
            )

            Spacer(Modifier.weight(1f))

            TotalBalanceChip(
                currency = currency,
                balance = balance,
                hideBalance = hideBalance,
                onClick = {
                    if (hideBalance) onHiddenBalanceClick() else onBalanceClick()
                },
            )
        }

        Spacer(Modifier.height(10.dp))

        BalanceRow(
            modifier = Modifier
                .clickableNoIndication(rememberInteractionSource()) {
                    if (hideBalance) {
                        onHiddenBalanceClick()
                    } else {
                        onBalanceClick()
                    }
                }
                .testTag("home_balance"),
            currency = currency,
            balance = net,
            textColor = White,
            balanceFontSize = 36.sp,
            shortenBigNumbers = true,
            hiddenMode = hideBalance,
            balanceAmountPrefix = if (net > 0) "+" else null,
        )

        if (trend.size >= 2) {
            Spacer(Modifier.height(12.dp))

            Sparkline(
                points = trend,
                lineColor = White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CashFlowStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.income),
                amount = monthlyIncome,
                currency = currency,
                arrowIcon = R.drawable.ic_trend_up,
                hidden = hideIncome,
                onClick = {
                    if (hideIncome) onHiddenIncomeClick() else onOpenIncomePieChart()
                },
            )

            Spacer(Modifier.width(12.dp))

            CashFlowStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.expenses),
                amount = monthlyExpenses.absoluteValue,
                currency = currency,
                arrowIcon = R.drawable.ic_trend_down,
                hidden = false,
                onClick = onOpenExpensePieChart,
            )
        }
    }
}

@Composable
private fun TotalBalanceChip(
    currency: String,
    balance: Double,
    hideBalance: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(HomeTheme.shapes.rFull)
            .background(White.copy(alpha = 0.15f))
            .clickableNoIndication(rememberInteractionSource()) {
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.total_balance),
            style = HomeTheme.typo.c.copy(
                color = White.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start,
            ),
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = if (hideBalance) "****" else balance.format(currency),
            style = HomeTheme.typo.c.copy(
                color = White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CashFlowStatCard(
    label: String,
    amount: Double,
    currency: String,
    @DrawableRes arrowIcon: Int,
    hidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(White.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ResourceIcon(
            modifier = Modifier.size(18.dp),
            icon = arrowIcon,
            tint = White,
        )

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = HomeTheme.typo.c.copy(
                    color = White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start,
                ),
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = if (hidden) {
                    "****"
                } else if (shouldShortAmount(amount)) {
                    shortenAmount(amount)
                } else {
                    amount.format(currency)
                },
                style = HomeTheme.typo.b2.copy(
                    color = White,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Sparkline(
    points: ImmutableList<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    val minValue = points.min()
    val maxValue = points.max()
    val range = (maxValue - minValue).takeIf { it != 0f } ?: 1f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val topPad = 8.dp.toPx()
        val usable = height - topPad
        val stepX = if (points.size > 1) width / (points.size - 1) else width

        fun yFor(value: Float) = topPad + (1f - (value - minValue) / range) * usable

        val linePath = Path()
        points.forEachIndexed { index, value ->
            val x = index * stepX
            val y = yFor(value)
            if (index == 0) {
                linePath.moveTo(x, y)
            } else {
                linePath.lineTo(x, y)
            }
        }

        val lastX = (points.size - 1) * stepX
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(lastX, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.3f),
                    lineColor.copy(alpha = 0f),
                ),
            ),
        )

        drawLine(
            color = lineColor.copy(alpha = 0.35f),
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
        )

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        val lastY = yFor(points.last())
        drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
        drawCircle(color = White, radius = 2.dp.toPx(), center = Offset(lastX, lastY))
    }
}

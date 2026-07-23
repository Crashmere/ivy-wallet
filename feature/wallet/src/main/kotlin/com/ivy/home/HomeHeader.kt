package com.ivy.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.ivy.ui.compose.verticalSwipeListener
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
) {
    Column {
        val percentExpanded by animateFloatAsState(
            targetValue = if (expanded) 1f else 0f,
            animationSpec = springBounce(
                stiffness = Spring.StiffnessLow
            ),
            label = "Home Header Expand Collapse"
        )

        Spacer(Modifier.height(20.dp))

        HeaderStickyRow(
            period = period,
            onShowMonthModal = onShowMonthModal,
            onSelectNextMonth = onSelectNextMonth,
            onSelectPreviousMonth = onSelectPreviousMonth,
        )

        Spacer(Modifier.height(16.dp))

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
) {
    val periodState = LocalPeriodState.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Spacer(Modifier.weight(1f))

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
            minWidth = 112.dp,
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

        Spacer(Modifier.width(12.dp))

        Spacer(Modifier.width(40.dp)) // settings menu button spacer
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
    onOpenMoreMenu: () -> Unit,
    onBalanceClick: () -> Unit,
    percentExpanded: Float,
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
            .verticalSwipeListener(
                sensitivity = Constants.SWIPE_DOWN_THRESHOLD_OPEN_MORE_MENU,
                state = rememberSwipeListenerState(),
                onSwipeDown = {
                    onOpenMoreMenu()
                },
            )
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

            Text(
                modifier = Modifier.clickableNoIndication(rememberInteractionSource()) {
                    if (hideBalance) onHiddenBalanceClick() else onBalanceClick()
                },
                text = if (hideBalance) {
                    "${stringResource(R.string.total_balance)} ****"
                } else {
                    "${stringResource(R.string.total_balance)} ${balance.format(currency)} $currency"
                },
                style = HomeTheme.typo.c.copy(
                    color = White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                ),
            )
        }

        Spacer(Modifier.height(8.dp))

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
            balanceFontSize = 34.sp,
            shortenBigNumbers = true,
            hiddenMode = hideBalance,
            balanceAmountPrefix = if (net > 0) "+" else null,
        )

        Spacer(Modifier.height(12.dp))

        if (trend.size >= 2) {
            Sparkline(
                points = trend,
                lineColor = White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            )

            Spacer(Modifier.height(14.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InlineStat(
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

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(White.copy(alpha = 0.25f))
            )

            InlineStat(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
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
private fun InlineStat(
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
            .clip(HomeTheme.shapes.rFull)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = HomeTheme.typo.c.copy(
                color = White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
            ),
        )

        Spacer(Modifier.width(6.dp))

        Text(
            modifier = Modifier.weight(1f, fill = false),
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

        Spacer(Modifier.width(4.dp))

        ResourceIcon(
            modifier = Modifier.size(16.dp),
            icon = arrowIcon,
            tint = White.copy(alpha = 0.85f),
        )
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

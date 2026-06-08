package com.ivy.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.compose.thenIf
import com.ivy.ui.period.LocalPeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.period.displayShort
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.drawColoredShadow
import com.ivy.data.model.currency.format
import com.ivy.ui.compose.horizontalSwipeListener
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.compose.rememberSwipeListenerState
import com.ivy.ui.animation.springBounce
import com.ivy.ui.compose.verticalSwipeListener
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.GradientGreen
import com.ivy.legacy.ui.theme.Green
import com.ivy.legacy.ui.theme.White
import com.ivy.legacy.ui.money.BalanceRow
import com.ivy.legacy.ui.icon.IvyIcon
import com.ivy.legacy.ui.button.IvyOutlinedButton
import com.ivy.legacy.ui.money.AmountCurrencyB1
import kotlin.math.absoluteValue

@ExperimentalAnimationApi
@Composable
internal fun HomeHeader(
    expanded: Boolean,
    period: TimePeriod,
    currency: String,
    balance: Double,
    onShowMonthModal: () -> Unit,
    onBalanceClick: () -> Unit,
    onSelectNextMonth: () -> Unit,
    hideBalance: Boolean,
    onHiddenBalanceClick: () -> Unit,
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
            percentExpanded = percentExpanded,
            period = period,
            currency = currency,
            balance = balance,
            hideBalance = hideBalance,

            onShowMonthModal = onShowMonthModal,
            onBalanceClick = onBalanceClick,
            onHiddenBalanceClick = onHiddenBalanceClick,
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
    percentExpanded: Float,
    period: TimePeriod,
    currency: String,
    balance: Double,
    onShowMonthModal: () -> Unit,
    onBalanceClick: () -> Unit,
    onSelectNextMonth: () -> Unit,
    hideBalance: Boolean,
    onHiddenBalanceClick: () -> Unit,
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
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            // Balance mini row
            if (percentExpanded < 1f) {
                BalanceRow(
                    modifier = Modifier
                        .alpha(alpha = 1f - percentExpanded)
                        .clickableNoIndication(rememberInteractionSource()) {
                            if (hideBalance) {
                                onHiddenBalanceClick()
                            } else {
                                onBalanceClick()
                            }
                        },
                    currency = currency,
                    balance = balance,
                    spacerCurrency = 8.dp,
                    currencyFontSize = 20.sp,
                    balanceFontSize = 22.sp,
                    shortenBigNumbers = true,
                    hiddenMode = hideBalance,
                    doubleRowDisplay = true,
                )
            }
        }

        IvyOutlinedButton(
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
            minWidth = 130.dp,
        ) {
            onShowMonthModal()
        }

        Spacer(Modifier.width(12.dp))

        Spacer(Modifier.width(40.dp)) // settings menu button spacer
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalSwipeListener(
                sensitivity = Constants.SWIPE_DOWN_THRESHOLD_OPEN_MORE_MENU,
                state = rememberSwipeListenerState(),
                onSwipeDown = {
                    onOpenMoreMenu()
                },
            ),
    ) {
        BalanceRow(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clickableNoIndication(rememberInteractionSource()) {
                    if (hideBalance) {
                        onHiddenBalanceClick()
                    } else {
                        onBalanceClick()
                    }
                }
                .testTag("home_balance"),
            currency = currency,
            balance = balance,
            shortenBigNumbers = true,
            hiddenMode = hideBalance
        )

        Spacer(modifier = Modifier.height(24.dp))

        IncomeExpenses(
            percentExpanded = percentExpanded,
            currency = currency,
            monthlyIncome = monthlyIncome,
            monthlyExpenses = monthlyExpenses,
            hideIncome = hideIncome,
            onHiddenIncomeClick = onHiddenIncomeClick,
            onOpenIncomePieChart = onOpenIncomePieChart,
            onOpenExpensePieChart = onOpenExpensePieChart
        )

        val cashflow = monthlyIncome - monthlyExpenses
        if (cashflow != 0.0 && !hideBalance) {
            Spacer(Modifier.height(12.dp))

            Text(
                modifier = Modifier.padding(
                    start = 24.dp,
                ),
                text = stringResource(
                    R.string.cashflow,
                    (if (cashflow > 0) "+" else ""),
                    cashflow.format(currency),
                    currency,
                ),
                style = LegacyTheme.typo.nB2.style(
                    color = if (cashflow < 0) LegacyTheme.colors.gray else Green,
                ),
            )

            Spacer(Modifier.height(4.dp))
        } else {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun IncomeExpenses(
    percentExpanded: Float,
    currency: String,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    hideIncome: Boolean,
    onHiddenIncomeClick: () -> Unit,
    onOpenIncomePieChart: () -> Unit,
    onOpenExpensePieChart: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(16.dp))

        HeaderCard(
            percentVisible = percentExpanded,
            icon = R.drawable.ic_income,
            backgroundGradient = GradientGreen,
            textColor = White,
            label = stringResource(R.string.income),
            currency = currency,
            amount = monthlyIncome,
            testTag = "home_card_income"
        ) {
            if (hideIncome) {
                onHiddenIncomeClick()
            } else {
                onOpenIncomePieChart()
            }
        }

        Spacer(Modifier.width(12.dp))

        HeaderCard(
            percentVisible = percentExpanded,
            icon = R.drawable.ic_expense,
            backgroundGradient = Gradient(LegacyTheme.colors.pureInverse, LegacyTheme.colors.gray),
            textColor = LegacyTheme.colors.pure,
            label = stringResource(R.string.expenses),
            currency = currency,
            amount = monthlyExpenses.absoluteValue,
            testTag = "home_card_expense",
        ) {
            onOpenExpensePieChart()
        }

        Spacer(Modifier.width(16.dp))
    }
}

@Composable
private fun RowScope.HeaderCard(
    @DrawableRes icon: Int,
    backgroundGradient: Gradient,
    percentVisible: Float,
    textColor: Color,
    label: String,
    currency: String,
    amount: Double,
    testTag: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .thenIf(percentVisible == 1f) {
                drawColoredShadow(backgroundGradient.startColor)
            }
            .clip(LegacyTheme.shapes.r4)
            .background(backgroundGradient.asHorizontalBrush())
            .testTag(testTag)
            .clickable(
                onClick = onClick,
            ),
    ) {
        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(16.dp))

            IvyIcon(
                icon = icon,
                tint = textColor,
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = label,
                style = LegacyTheme.typo.c.style(
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold,
                ),
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(20.dp))

            AmountCurrencyB1(
                amount = amount,
                currency = currency,
                textColor = textColor,
                shortenBigNumbers = true,
            )

            Spacer(Modifier.width(4.dp))
        }

        Spacer(Modifier.height(20.dp))
    }
}

package com.ivy.home

import android.animation.ArgbEvaluator
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ivy.ui.compose.BackPressHandler
import com.ivy.ui.compose.thenIf
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.animation.lerp
import com.ivy.ui.compose.navigationBarInset
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.compose.rememberSwipeListenerState
import com.ivy.ui.animation.springBounce
import com.ivy.ui.compose.statusBarInset
import com.ivy.ui.compose.toDensityPx
import com.ivy.ui.compose.verticalSwipeListener
import com.ivy.ui.R
import com.ivy.ui.money.AmountCurrencyB1
import com.ivy.ui.compose.FilledIconButton
import com.ivy.ui.compose.ResourceIcon
import kotlin.math.roundToInt

private const val SWIPE_UP_THRESHOLD_CLOSE_MORE_MENU = 300

private fun colorLerp(start: Color, end: Color, fraction: Float): Color {
    return Color(ArgbEvaluator().evaluate(fraction, start.toArgb(), end.toArgb()) as Int)
}

internal enum class MoreMenuDestination {
    Search,
    Settings,
    Categories,
    PlannedPayments,
    Reports,
    Budgets,
    Loans,
    BulkEdit
}

@Composable
internal fun BoxWithConstraintsScope.MoreMenu(
    expanded: Boolean,

    balance: Double,
    buffer: Double,
    currency: String,

    showPlannedPaymentsQuickAccess: Boolean,
    showBudgetsQuickAccess: Boolean,
    showLoansQuickAccess: Boolean,

    setExpanded: (Boolean) -> Unit,
    onBufferClick: () -> Unit,
    onCurrencyClick: () -> Unit,
    onDestinationClick: (MoreMenuDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val percentExpanded by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = springBounce(),
        label = ""
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) -180f else 0f,
        animationSpec = springBounce(),
        label = ""
    )

    val buttonSizePx = 40.dp.toDensityPx()
    val screenWidthPx = maxWidth.toDensityPx()
    val screenHeightPx = maxHeight.toDensityPx()

    val xBase = screenWidthPx - 24.dp.toDensityPx()
    val yBaseCollapsed = 20.dp.toDensityPx() + statusBarInset()
    val yBaseExpanded = screenHeightPx - 48.dp.toDensityPx() - navigationBarInset()

    val yButton = lerp(
        start = yBaseCollapsed,
        end = yBaseExpanded - buttonSizePx,
        fraction = percentExpanded
    )

    // Background
    val colorMedium = HomeTheme.colors.medium
    if (percentExpanded > 0.01f) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .clickableNoIndication(rememberInteractionSource()) {
                    // do nothing
                }
                .zIndex(500f)
        ) {
            val radiusCollapsed = buttonSizePx / 2f
            val radiusExpanded = screenHeightPx * 1.5f
            val radius = lerp(radiusCollapsed, radiusExpanded, percentExpanded)

            val yBackground = lerp(
                start = yBaseCollapsed + radius,
                end = yBaseExpanded,
                fraction = percentExpanded
            )

            drawCircle(
                color = colorMedium,
                center = Offset(
                    x = xBase - buttonSizePx / 2f,
                    y = yBackground
                ),
                radius = radius
            )
        }
    }

    if (percentExpanded > 0.01f) {
        Column(
            modifier = modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .fillMaxSize()
                .alpha(percentExpanded)
                .verticalScroll(rememberScrollState())
                .zIndex(510f)
                .verticalSwipeListener(
                    sensitivity = SWIPE_UP_THRESHOLD_CLOSE_MORE_MENU,
                    state = rememberSwipeListenerState(),
                    onSwipeUp = {
                        setExpanded(false)
                    }
                )
        ) {
            BackPressHandler(enabled = expanded) {
                setExpanded(false)
            }

            Content(
                balance = balance,
                buffer = buffer,
                currency = currency,
                showPlannedPaymentsQuickAccess = showPlannedPaymentsQuickAccess,
                showBudgetsQuickAccess = showBudgetsQuickAccess,
                showLoansQuickAccess = showLoansQuickAccess,
                onBufferClick = onBufferClick,
                onCurrencyClick = onCurrencyClick,
                onDestinationClick = onDestinationClick
            )
        }
    }

    FilledIconButton(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = xBase.roundToInt() - buttonSizePx.roundToInt(),
                        y = yButton.roundToInt()
                    )
                }
            }
            .rotate(iconRotation)
            .thenIf(expanded) {
                zIndex(520f)
            }
            .testTag("home_more_menu_arrow"),
        backgroundColor = colorLerp(HomeTheme.colors.medium, HomeTheme.colors.pure, percentExpanded),
        tint = HomeTheme.colors.pureInverse,
        icon = R.drawable.ic_expandarrow
    ) {
        setExpanded(!expanded)
    }
}

@Composable
private fun ColumnScope.Content(
    balance: Double,
    buffer: Double,
    currency: String,

    showPlannedPaymentsQuickAccess: Boolean,
    showBudgetsQuickAccess: Boolean,
    showLoansQuickAccess: Boolean,

    onBufferClick: () -> Unit,
    onCurrencyClick: () -> Unit,
    onDestinationClick: (MoreMenuDestination) -> Unit,
) {
    Spacer(Modifier.height(24.dp))

    SearchButton {
        onDestinationClick(MoreMenuDestination.Search)
    }

    Spacer(Modifier.height(16.dp))

    QuickAccess(
        showPlannedPayments = showPlannedPaymentsQuickAccess,
        showBudgets = showBudgetsQuickAccess,
        showLoans = showLoansQuickAccess,
        onDestinationClick = onDestinationClick
    )

    Spacer(Modifier.height(40.dp))

    Buffer(
        buffer = buffer,
        currency = currency,
        balance = balance,
        onBufferClick = onBufferClick
    )

    Spacer(Modifier.height(16.dp))

    Spacer(Modifier.weight(1f))
}

@Composable
private fun SearchButton(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(HomeTheme.shapes.rFull)
            .background(HomeTheme.colors.pure)
            .border(1.dp, HomeTheme.colors.gray, HomeTheme.shapes.rFull)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))

        ResourceIcon(
            icon = R.drawable.ic_search,
            tint = HomeTheme.colors.pureInverse
        )

        Spacer(Modifier.width(12.dp))

        Text(
            modifier = Modifier.padding(
                vertical = 12.dp,
            ),
            text = stringResource(R.string.search_transactions),
            style = HomeTheme.typo.b2.copy(
                fontWeight = FontWeight.SemiBold,
                color = HomeTheme.colors.pureInverse,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.width(16.dp))
    }
}

@Composable
private fun ColumnScope.Buffer(
    buffer: Double,
    currency: String,
    balance: Double,
    onBufferClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoIndication(rememberInteractionSource()) {
                onBufferClick()
            }
            .testTag("savings_goal_row"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        Text(
            text = stringResource(R.string.savings_goal),
            style = HomeTheme.typo.b1.copy(
                color = HomeTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.weight(1f))

        AmountCurrencyB1(
            amount = buffer,
            currency = currency,
            amountFontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.width(32.dp))
    }

    Spacer(Modifier.height(12.dp))

    HomeBufferBattery(
        modifier = Modifier.padding(horizontal = 16.dp),
        buffer = buffer,
        currency = currency,
        balance = balance,
    ) {
        onBufferClick()
    }
}

private data class QuickAccessItem(
    @DrawableRes val icon: Int,
    val label: String,
    val destination: MoreMenuDestination,
)

@Composable
private fun QuickAccess(
    showPlannedPayments: Boolean,
    showBudgets: Boolean,
    showLoans: Boolean,
    onDestinationClick: (MoreMenuDestination) -> Unit
) {
    val categoriesLabel = stringResource(R.string.categories)
    val plannedPaymentsLabel = stringResource(R.string.planned_payments)
    val budgetsLabel = stringResource(R.string.budgets)
    val loansLabel = stringResource(R.string.loans)

    val items = buildList {
        add(QuickAccessItem(R.drawable.home_more_menu_categories, categoriesLabel, MoreMenuDestination.Categories))
        add(QuickAccessItem(R.drawable.home_more_menu_bulk_edit, "批量修改", MoreMenuDestination.BulkEdit))
        if (showPlannedPayments) {
            add(
                QuickAccessItem(
                    R.drawable.home_more_menu_planned_payments,
                    plannedPaymentsLabel,
                    MoreMenuDestination.PlannedPayments
                )
            )
        }
        if (showBudgets) {
            add(QuickAccessItem(R.drawable.home_more_menu_budgets, budgetsLabel, MoreMenuDestination.Budgets))
        }
        if (showLoans) {
            add(QuickAccessItem(R.drawable.home_more_menu_loans, loansLabel, MoreMenuDestination.Loans))
        }
    }

    Column {
        Text(
            modifier = Modifier.padding(start = 24.dp),
            text = stringResource(R.string.quick_access),
            style = HomeTheme.typo.b2.copy(
                color = HomeTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )

        items.chunked(4).forEach { rowItems ->
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                Spacer(Modifier.weight(1f))

                rowItems.forEach { item ->
                    MoreMenuButton(
                        icon = item.icon,
                        label = item.label
                    ) {
                        onDestinationClick(item.destination)
                    }

                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MoreMenuButton(
    @DrawableRes icon: Int,
    label: String,

    backgroundColor: Color = HomeTheme.colors.pure,
    tint: Color = HomeTheme.colors.pureInverse,
    expandPadding: Dp = 14.dp,

    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconButton(
            icon = icon,
            backgroundColor = backgroundColor,
            tint = tint,
            clickAreaPadding = expandPadding,
            onClick = onClick
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier
                .defaultMinSize(minWidth = 92.dp)
                .clickableNoIndication(rememberInteractionSource()) {
                    onClick()
                },
            text = label,
            style = HomeTheme.typo.c.copy(
                color = HomeTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        )
    }
}

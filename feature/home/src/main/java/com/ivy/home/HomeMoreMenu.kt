package com.ivy.home

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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ivy.base.theme.Theme
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.theme.system.style
import com.ivy.ui.compose.thenIf
import com.ivy.legacy.ui.clickableNoIndication
import com.ivy.legacy.ui.colorLerp
import com.ivy.legacy.ui.lerp
import com.ivy.legacy.ui.navigationBarInset
import com.ivy.legacy.ui.rememberInteractionSource
import com.ivy.legacy.ui.rememberSwipeListenerState
import com.ivy.legacy.ui.springBounce
import com.ivy.legacy.ui.statusBarInset
import com.ivy.legacy.ui.toDensityPx
import com.ivy.legacy.ui.verticalSwipeListener
import com.ivy.navigation.BudgetScreen
import com.ivy.navigation.CategoriesScreen
import com.ivy.navigation.LoansScreen
import com.ivy.navigation.PlannedPaymentsScreen
import com.ivy.navigation.ReportScreen
import com.ivy.navigation.SearchScreen
import com.ivy.navigation.SettingsScreen
import com.ivy.navigation.navigation
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Gray
import com.ivy.legacy.ui.component.BufferBattery
import com.ivy.legacy.ui.component.CircleButtonFilled
import com.ivy.legacy.ui.component.IvyIcon
import com.ivy.legacy.ui.modal.AddModalBackHandling
import com.ivy.legacy.ui.component.AmountCurrencyB1
import java.util.UUID
import kotlin.math.roundToInt

private const val SWIPE_UP_THRESHOLD_CLOSE_MORE_MENU = 300

@Composable
fun BoxWithConstraintsScope.MoreMenu(
    expanded: Boolean,

    balance: Double,
    buffer: Double,
    currency: String,
    theme: Theme,

    setExpanded: (Boolean) -> Unit,
    onSwitchTheme: () -> Unit,
    onBufferClick: () -> Unit,
    onCurrencyClick: () -> Unit,
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
    val colorMedium = LegacyTheme.colors.medium
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
            val modalId = remember {
                UUID.randomUUID()
            }

            AddModalBackHandling(
                modalId = modalId,
                visible = expanded
            ) {
                setExpanded(false)
            }

            Content(
                theme = theme,
                onSwitchTheme = onSwitchTheme,
                balance = balance,
                buffer = buffer,
                currency = currency,
                onBufferClick = onBufferClick,
                onCurrencyClick = onCurrencyClick
            )
        }
    }

    CircleButtonFilled(
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
        backgroundColor = colorLerp(LegacyTheme.colors.medium, LegacyTheme.colors.pure, percentExpanded),
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
    theme: Theme,

    onSwitchTheme: () -> Unit,
    onBufferClick: () -> Unit,
    onCurrencyClick: () -> Unit,
) {
    Spacer(Modifier.height(24.dp))

    val nav = navigation()
    SearchButton {
        nav.navigateTo(
            screen = SearchScreen
        )
    }

    Spacer(Modifier.height(16.dp))

    QuickAccess(
        theme = theme,
        onSwitchTheme = onSwitchTheme
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
            .clip(LegacyTheme.shapes.rFull)
            .background(LegacyTheme.colors.pure)
            .border(1.dp, Gray, LegacyTheme.shapes.rFull)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))

        IvyIcon(icon = R.drawable.ic_search)

        Spacer(Modifier.width(12.dp))

        Text(
            modifier = Modifier.padding(
                vertical = 12.dp,
            ),
            text = stringResource(R.string.search_transactions),
            style = LegacyTheme.typo.b2.style(
                fontWeight = FontWeight.SemiBold,
                color = LegacyTheme.colors.pureInverse
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
            style = LegacyTheme.typo.b1.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold
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

    BufferBattery(
        modifier = Modifier.padding(horizontal = 16.dp),
        buffer = buffer,
        currency = currency,
        balance = balance,
    ) {
        onBufferClick()
    }
}

@Composable
private fun QuickAccess(
    theme: Theme,
    onSwitchTheme: () -> Unit
) {
    Column {
        val nav = navigation()

        Text(
            modifier = Modifier.padding(start = 24.dp),
            text = stringResource(R.string.quick_access),
            style = LegacyTheme.typo.b2.style()
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Spacer(Modifier.weight(1f))

            MoreMenuButton(
                icon = R.drawable.home_more_menu_settings,
                label = stringResource(R.string.settings)
            ) {
                nav.navigateTo(SettingsScreen)
            }

            Spacer(Modifier.weight(1f))

            MoreMenuButton(
                icon = R.drawable.home_more_menu_categories,
                label = stringResource(R.string.categories)
            ) {
                nav.navigateTo(CategoriesScreen)
            }

            Spacer(Modifier.weight(1f))

            MoreMenuButton(
                icon = when (theme) {
                    Theme.LIGHT -> R.drawable.home_more_menu_light_mode
                    Theme.DARK -> R.drawable.home_more_menu_dark_mode
                    Theme.AMOLED_DARK -> R.drawable.home_more_menu_amoled_dark_mode
                    Theme.AUTO -> R.drawable.home_more_menu_auto_mode
                },
                label = when (theme) {
                    Theme.LIGHT -> stringResource(R.string.light_mode)
                    Theme.DARK -> stringResource(R.string.dark_mode)
                    Theme.AMOLED_DARK -> stringResource(R.string.amoled_mode)
                    Theme.AUTO -> stringResource(R.string.auto_mode)
                },
                backgroundColor = when (theme) {
                    Theme.LIGHT -> LegacyTheme.colors.pure
                    Theme.DARK -> LegacyTheme.colors.pureInverse
                    Theme.AMOLED_DARK -> LegacyTheme.colors.pureInverse
                    Theme.AUTO -> LegacyTheme.colors.pure
                },
                tint = when (theme) {
                    Theme.LIGHT -> LegacyTheme.colors.pureInverse
                    Theme.DARK -> LegacyTheme.colors.pure
                    Theme.AMOLED_DARK -> LegacyTheme.colors.pure
                    Theme.AUTO -> LegacyTheme.colors.pureInverse
                }
            ) {
                onSwitchTheme()
            }

            Spacer(Modifier.weight(1f))

            MoreMenuButton(
                icon = R.drawable.home_more_menu_planned_payments,
                label = stringResource(R.string.planned_payments)
            ) {
                nav.navigateTo(PlannedPaymentsScreen)
            }

            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // Second Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Spacer(Modifier.weight(1f))

            MoreMenuButton(
                icon = R.drawable.home_more_menu_reports,
                label = stringResource(R.string.reports),
            ) {
                nav.navigateTo(ReportScreen)
            }

            Spacer(Modifier.weight(1f))

            MoreMenuButton(
                icon = R.drawable.home_more_menu_budgets,
                label = stringResource(R.string.budgets),
            ) {
                nav.navigateTo(BudgetScreen)
            }

            Spacer(Modifier.weight(1f))

            MoreMenuButton(
                icon = R.drawable.home_more_menu_loans,
                label = stringResource(R.string.loans),
            ) {
                nav.navigateTo(LoansScreen)
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun MoreMenuButton(
    @DrawableRes icon: Int,
    label: String,

    backgroundColor: Color = LegacyTheme.colors.pure,
    tint: Color = LegacyTheme.colors.pureInverse,
    expandPadding: Dp = 14.dp,

    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircleButtonFilled(
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
            style = LegacyTheme.typo.c.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        )
    }
}

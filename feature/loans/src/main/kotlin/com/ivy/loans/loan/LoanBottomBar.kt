package com.ivy.loans.loan

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.compose.navigationBarInset
import com.ivy.ui.compose.toDensityPx
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.legacy.ui.button.IvyCircleButton
import com.ivy.legacy.ui.icon.IvyIcon
import kotlin.math.roundToInt

private val FabButtonSize = 56.dp
private const val ZIndex = 200f
private val PendingLoanColor = Color(0xFFA020F0)
private val AddLoanGradient = Gradient(PendingLoanColor, Color(0xFFED3EF7))

@Composable
internal fun BoxWithConstraintsScope.LoanBottomBar(
    tab: LoanTab,
    selectTab: (LoanTab) -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(LegacyTheme.colors.pure.copy(alpha = 0.95f))
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Tab(
            icon = R.drawable.ic_custom_loan_s,
            name = "Pending",
            selected = tab == LoanTab.PENDING,
            selectedColor = PendingLoanColor
        ) {
            selectTab(LoanTab.PENDING)
        }

        Spacer(Modifier.width(FabButtonSize))

        Tab(
            icon = R.drawable.ic_custom_loan_s,
            name = "Completed",
            selected = tab == LoanTab.COMPLETED,
            selectedColor = LegacyTheme.colors.green
        ) {
            selectTab(LoanTab.COMPLETED)
        }
    }

    val screenWidthPx = maxWidth.toDensityPx()
    val screenHeightPx = maxHeight.toDensityPx()
    val fabStartX = screenWidthPx / 2 - FabButtonSize.toDensityPx() / 2
    val fabStartY = screenHeightPx - navigationBarInset() -
            30.dp.toDensityPx() - FabButtonSize.toDensityPx()

    IvyCircleButton(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = fabStartX.roundToInt(),
                        y = fabStartY.roundToInt()
                    )
                }
            }
            .size(FabButtonSize)
            .zIndex(ZIndex),
        backgroundPadding = 8.dp,
        icon = R.drawable.ic_add,
        backgroundGradient = AddLoanGradient,
        hasShadow = true,
        tint = White
    ) {
        onAdd()
    }
}
@Composable
private fun RowScope.Tab(
    @DrawableRes icon: Int,
    name: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .clip(LegacyTheme.shapes.rFull)
            .clickable(onClick = onClick)
            .padding(top = 12.dp, bottom = 16.dp)
            .testTag(name.lowercase()),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IvyIcon(
            icon = icon,
            tint = if (selected) selectedColor else LegacyTheme.colors.pureInverse
        )

        if (selected) {
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = name,
                style = LegacyTheme.typo.c.style(
                    fontWeight = FontWeight.Bold,
                    color = selectedColor
                )
            )
        }
    }
}

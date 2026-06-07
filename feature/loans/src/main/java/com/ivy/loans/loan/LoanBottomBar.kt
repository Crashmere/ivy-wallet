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
import com.ivy.design.l0_system.LegacyTheme
import com.ivy.design.l0_system.style
import com.ivy.ui.legacy.navigationBarInset
import com.ivy.ui.legacy.toDensityPx
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.GradientPurple
import com.ivy.legacy.ui.theme.Green
import com.ivy.legacy.ui.theme.Purple
import com.ivy.legacy.ui.theme.White
import com.ivy.legacy.ui.component.IvyCircleButton
import com.ivy.legacy.ui.component.IvyIcon
import com.ivy.legacy.ui.theme.pureBlur
import kotlin.math.roundToInt

val FAB_BUTTON_SIZE = 56.dp
const val ZINDEX = 200f

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
            .background(pureBlur())
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Tab(
            icon = R.drawable.ic_custom_loan_s,
            name = "Pending",
            selected = tab == LoanTab.PENDING,
            selectedColor = Purple
        ) {
            selectTab(LoanTab.PENDING)
        }

        Spacer(Modifier.width(FAB_BUTTON_SIZE))

        Tab(
            icon = R.drawable.ic_custom_loan_s,
            name = "Completed",
            selected = tab == LoanTab.COMPLETED,
            selectedColor = Green
        ) {
            selectTab(LoanTab.COMPLETED)
        }
    }

    val screenWidthPx = maxWidth.toDensityPx()
    val screenHeightPx = maxHeight.toDensityPx()
    val fabStartX = screenWidthPx / 2 - FAB_BUTTON_SIZE.toDensityPx() / 2
    val fabStartY = screenHeightPx - navigationBarInset() -
            30.dp.toDensityPx() - FAB_BUTTON_SIZE.toDensityPx()

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
            .size(FAB_BUTTON_SIZE)
            .zIndex(ZINDEX),
        backgroundPadding = 8.dp,
        icon = R.drawable.ic_add,
        backgroundGradient = GradientPurple,
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

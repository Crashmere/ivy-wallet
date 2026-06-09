package com.ivy.planned.list

import com.ivy.planned.PlannedTheme

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.ui.compose.navigationBarInset
import com.ivy.ui.compose.toDensityDp
import com.ivy.ui.R
import com.ivy.ui.compose.CloseIconButton
import com.ivy.ui.compose.gradientCutBackgroundTop
import com.ivy.ui.compose.OutlinedPillButton

@Composable
internal fun BoxWithConstraintsScope.PlannedPaymentsBottomBar(
    bottomInset: Dp = navigationBarInset().toDensityDp(),
    onClose: () -> Unit,
    onAdd: () -> Unit
) {
    PlannedPaymentsActionsRow(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .gradientCutBackgroundTop(PlannedTheme.colors.pure, LocalDensity.current)
            .padding(bottom = bottomInset)
            .padding(bottom = 24.dp)
    ) {
        Spacer(Modifier.width(20.dp))

        CloseButton {
            onClose()
        }

        Spacer(Modifier.weight(1f))

        OutlinedPillButton(
            iconStart = R.drawable.ic_planned_payments,
            text = stringResource(R.string.add_payment),
            shape = PlannedTheme.shapes.rFull,
            solidBackground = true,
            backgroundColor = PlannedTheme.colors.pure,
            iconTint = PlannedTheme.colors.pureInverse,
            borderColor = PlannedTheme.colors.medium,
            textStyle = PlannedTheme.typo.b2.copy(
                fontWeight = FontWeight.Bold,
                color = PlannedTheme.colors.pureInverse,
                textAlign = TextAlign.Start,
            ),
        ) {
            onAdd()
        }

        Spacer(Modifier.width(20.dp))
    }
}

@Composable
private fun PlannedPaymentsActionsRow(
    modifier: Modifier = Modifier,
    lineColor: Color = PlannedTheme.colors.medium,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = lineColor,
                    strokeWidth = 2.dp.toPx(),
                    start = Offset(
                        x = 0f,
                        y = size.height / 2
                    ),
                    end = Offset(
                        x = size.width,
                        y = size.height / 2
                    )
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
private fun CloseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CloseIconButton(
        modifier = modifier,
        backgroundColor = PlannedTheme.colors.pure,
        borderColor = PlannedTheme.colors.medium,
        tint = PlannedTheme.colors.pureInverse,
        onClick = onClick,
    )
}

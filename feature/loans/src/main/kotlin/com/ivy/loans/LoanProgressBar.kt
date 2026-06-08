package com.ivy.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import com.ivy.ui.theme.colors.IvyFixedColors.Ivy
import com.ivy.legacy.ui.theme.LegacyTheme

@Composable
internal fun LoanProgressBar(
    modifier: Modifier = Modifier,
    notFilledColor: Color = LegacyTheme.colors.pure,
    positiveProgress: Boolean = true,
    percent: Double
) {
    val green = LegacyTheme.colors.green
    val orange = LegacyTheme.colors.orange
    val red = LegacyTheme.colors.red

    Spacer(
        modifier = modifier
            .clip(LegacyTheme.shapes.r4)
            .background(notFilledColor)
            .drawBehind {
                drawRect(
                    color = when {
                        percent <= 0.25 -> if (positiveProgress) red else green
                        percent <= 0.50 -> if (positiveProgress) orange else Ivy
                        percent <= 0.75 -> if (positiveProgress) Ivy else orange
                        else -> if (positiveProgress) green else red
                    },
                    size = size.copy(
                        width = (size.width * percent).toFloat()
                    )
                )
            },
    )
}

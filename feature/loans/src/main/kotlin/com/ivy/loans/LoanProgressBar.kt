package com.ivy.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import com.ivy.legacy.ui.theme.Green
import com.ivy.legacy.ui.theme.Ivy
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.Orange

@Composable
internal fun LoanProgressBar(
    modifier: Modifier = Modifier,
    notFilledColor: Color = LegacyTheme.colors.pure,
    positiveProgress: Boolean = true,
    percent: Double
) {
    val red = LegacyTheme.colors.red

    Spacer(
        modifier = modifier
            .clip(LegacyTheme.shapes.r4)
            .background(notFilledColor)
            .drawBehind {
                drawRect(
                    color = when {
                        percent <= 0.25 -> if (positiveProgress) red else Green
                        percent <= 0.50 -> if (positiveProgress) Orange else Ivy
                        percent <= 0.75 -> if (positiveProgress) Ivy else Orange
                        else -> if (positiveProgress) Green else red
                    },
                    size = size.copy(
                        width = (size.width * percent).toFloat()
                    )
                )
            },
    )
}

package com.ivy.wallet.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import com.ivy.design.l0_system.LegacyTheme
import com.ivy.wallet.ui.theme.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    notFilledColor: Color = LegacyTheme.colors.pure,
    positiveProgress: Boolean = true,
    percent: Double
) {
    Spacer(
        modifier = modifier
            .clip(LegacyTheme.shapes.r4)
            .background(notFilledColor)
            .drawBehind {
                drawRect(
                    color = when {
                        percent <= 0.25 -> {
                            if (positiveProgress) Red else Green
                        }
                        percent <= 0.50 -> {
                            if (positiveProgress) Orange else Ivy
                        }
                        percent <= 0.75 -> {
                            if (positiveProgress) Ivy else Orange
                        }
                        else -> if (positiveProgress) Green else Red
                    },
                    size = size.copy(
                        width = (size.width * percent).toFloat()
                    )
                )
            },
    )
}
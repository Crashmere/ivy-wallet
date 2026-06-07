package com.ivy.legacy.ui.theme.system

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun TextStyle.colorAs(color: Color) = this.copy(color = color)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun TextStyle.style(
    color: Color = LegacyTheme.colors.pureInverse,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign = TextAlign.Start
) = this.copy(
    color = color,
    fontWeight = fontWeight,
    textAlign = textAlign
)

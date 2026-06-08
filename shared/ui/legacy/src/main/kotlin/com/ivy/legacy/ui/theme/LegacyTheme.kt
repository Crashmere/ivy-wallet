package com.ivy.legacy.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import com.ivy.legacy.ui.theme.system.LegacyTheme as SystemLegacyTheme

object LegacyTheme {
    val colors
        @Composable
        @ReadOnlyComposable
        get() = SystemLegacyTheme.colors

    val typo
        @Composable
        @ReadOnlyComposable
        get() = SystemLegacyTheme.typo

    val shapes
        @Composable
        @ReadOnlyComposable
        get() = SystemLegacyTheme.shapes
}

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

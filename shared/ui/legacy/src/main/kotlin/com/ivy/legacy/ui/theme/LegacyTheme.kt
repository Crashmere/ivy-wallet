package com.ivy.legacy.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
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

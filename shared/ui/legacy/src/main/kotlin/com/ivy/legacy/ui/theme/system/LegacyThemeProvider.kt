package com.ivy.legacy.ui.theme.system

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.ivy.data.model.Theme
import com.ivy.ui.platform.findActivity
import com.ivy.ui.theme.IvyMaterial3Theme

private val LocalIvyColors = compositionLocalOf<IvyColors> { error("No IvyColors") }

private val LocalIvyTypography = compositionLocalOf<IvyTypography> { error("No IvyTypography") }

private val LocalIvyShapes = compositionLocalOf<IvyShapes> { error("No IvyShapes") }

internal object LegacyThemeValues {
    val colors: IvyColors
        @Composable
        @ReadOnlyComposable
        get() = LocalIvyColors.current

    val typo: IvyTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalIvyTypography.current

    val shapes: IvyShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalIvyShapes.current
}

@Composable
internal fun LegacyThemeProvider(
    theme: Theme,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = LegacyThemeDefaults.colors(theme, isDarkTheme)
    val typography = LegacyThemeDefaults.typography()
    val shapes = LegacyThemeDefaults.shapes()

    CompositionLocalProvider(
        LocalIvyColors provides colors,
        LocalIvyTypography provides typography,
        LocalIvyShapes provides shapes
    ) {
        val view = LocalView.current
        val activity = view.context.findActivity()
        if (!view.isInEditMode && activity != null) {
            SideEffect {
                val window = activity.window
                window.statusBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    colors.isLight
            }
        }

        IvyMaterial3Theme(
            dark = !colors.isLight,
            isTrueBlack = theme == Theme.AMOLED_DARK,
            content = content,
        )
    }
}

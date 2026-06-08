package com.ivy.legacy.ui.theme

import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ivy.ui.theme.colors.dynamicContrast as coreDynamicContrast
import com.ivy.ui.theme.colors.isDarkColor as coreIsDarkColor
import com.ivy.ui.theme.colors.toComposeColor as coreToComposeColor

internal typealias Gradient = com.ivy.ui.theme.colors.Gradient

@Composable
internal fun mediumBlur() = LegacyTheme.colors.medium.copy(alpha = 0.95f)

@Composable
internal fun gradientExpenses() = Gradient(LegacyTheme.colors.pureInverse, LegacyTheme.colors.gray)

@Composable
internal fun gradientBlack() = Gradient(LegacyTheme.colors.gray, LegacyTheme.colors.pureInverse)

internal fun findContrastTextColor(backgroundColor: Color) =
    com.ivy.ui.theme.colors.findContrastTextColor(backgroundColor)

internal fun isDarkColor(color: Color) = coreIsDarkColor(color)

internal fun isDarkColor(@ColorInt color: Int) =
    com.ivy.ui.theme.colors.isDarkColor(color)

internal fun Color.dynamicContrast() =
    coreDynamicContrast()

internal fun Int.toComposeColor() =
    coreToComposeColor()

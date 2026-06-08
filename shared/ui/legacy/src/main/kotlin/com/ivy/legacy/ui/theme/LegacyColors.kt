package com.ivy.legacy.ui.theme

import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.ui.theme.colors.dynamicContrast as coreDynamicContrast
import com.ivy.ui.theme.colors.isDarkColor as coreIsDarkColor
import com.ivy.ui.theme.colors.toComposeColor as coreToComposeColor

internal typealias Gradient = com.ivy.ui.theme.colors.Gradient

internal val White = Color(0xFFFAFAFA)

internal val Black = Color(0xFF111114)

// Primary
internal val Ivy = Color(0xFF6B4DFF)

internal val Blue = Color(0xFF4CC3FF)

internal val Green = Color(0xFF14CC9E)

internal val Orange = Color(0xFFF29F30)

internal val Orange3 = Color(0xFFFFC34C)

internal val Red = Color(0xFFFF4060)

internal val Red3 = Color(0xFFFF4CA6)

// Light
internal val IvyLight = Color(0xFFD5CCFF)

internal val BlueLight = Color(0xFFB3E6FF)

internal val GreenLight = Color(0xFFAAF2E0)

internal val OrangeLight = Color(0xFFFFDEB3)

internal val RedLight = Color(0xFFFFCCD5)

internal val Red3Light = Color(0xFFFFCCE6)

// Dark
internal val IvyDark = Color(0xFF352680)

internal val Purple1Dark = Color(0xFF622680)

internal val Purple = Color(0xFFA020F0)

internal val Blue2Dark = Color(0xFF227373)

internal val GreenDark = Color(0xFF0A664F)

internal val OrangeDark = Color(0xFF734B17)
// --------------------------------------------------------------------------------------------------

internal val MediumBlack = Color(0xFF2B2C2D)

internal val Gray = Color(0xFF939199)

internal val MediumWhite = Color(0xFFEFEEF0)

internal val GradientRed = Gradient(Red, Color(0xFFFF99AB))

internal val GradientGreen = IvyGradients.Green

internal val GradientOrangeRevert = Gradient(Color(0xFFF2CD9E), Orange)

internal val GradientIvy = IvyGradients.Ivy

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

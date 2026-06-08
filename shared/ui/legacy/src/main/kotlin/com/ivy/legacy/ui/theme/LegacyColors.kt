package com.ivy.legacy.ui.theme

import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils

val White = Color(0xFFFAFAFA)

internal val Black = Color(0xFF111114)

// Primary
val Ivy = Color(0xFF6B4DFF)

internal val Blue = Color(0xFF4CC3FF)

val Green = Color(0xFF14CC9E)

val Orange = Color(0xFFF29F30)

internal val Orange3 = Color(0xFFFFC34C)

val Red = Color(0xFFFF4060)

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

val Gray = Color(0xFF939199)

internal val MediumWhite = Color(0xFFEFEEF0)

internal val Transparent = Color(0x00000000)

internal val GradientRed = Gradient(Red, Color(0xFFFF99AB))

internal val GradientPurple = Gradient(Purple, Color(0xFFED3EF7))

val GradientGreen = Gradient(Green, Color(0xFF49F2C8))

internal val GradientOrangeRevert = Gradient(Color(0xFFF2CD9E), Orange)

val GradientIvy = Gradient(Ivy, Color(0xFFAA99FF))

fun Modifier.gradientCutBackgroundTop(
    pure: Color,
    density: Density,
    endY: Dp = 32.dp
): Modifier {
    return background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Transparent,
                pure,
            ),
            endY = with(density) {
                endY.toPx()
            }
        )
    ).padding(top = 16.dp)
}

internal fun Modifier.gradientCutBackgroundBottom(
    pure: Color,
    paddingBottom: Dp,
): Modifier {
    return background(
        brush = Brush.verticalGradient(
            colors = listOf(
                pure,
                Transparent
            ),
        )
    ).padding(bottom = paddingBottom)
}

@Composable
internal fun mediumBlur() = LegacyTheme.colors.medium.copy(alpha = 0.95f)

@Composable
internal fun gradientExpenses() = Gradient(LegacyTheme.colors.pureInverse, LegacyTheme.colors.gray)

data class Gradient(
    val startColor: Color,
    val endColor: Color
) {
    companion object {
        fun solid(color: Color) = Gradient(color, color)

        @Composable
        fun black() = Gradient(LegacyTheme.colors.gray, LegacyTheme.colors.pureInverse)
    }

    fun asHorizontalBrush() = Brush.horizontalGradient(colors = listOf(startColor, endColor))

    internal fun asVerticalBrush() = Brush.verticalGradient(colors = listOf(startColor, endColor))
}

internal fun Color.asBrush(): Brush {
    return Brush.linearGradient(colors = listOf(this, this))
}

fun findContrastTextColor(backgroundColor: Color): Color {
    return if (isDarkColor(backgroundColor.toArgb())) White else Black
}

fun isDarkColor(color: Color): Boolean {
    return isDarkColor(color.toArgb())
}

internal fun isDarkColor(@ColorInt color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) <= 0.5
}

fun Color.dynamicContrast(): Color {
    val pickedColor = this.toHSVSpec()

    return when {
        pickedColor.s >= 0.5f && pickedColor.v >= 0.4f -> {
            // Primary
            if (isDarkColor(this)) {
                lighten()
            } else {
                darken()
            }
        }

        pickedColor.s <= 0.5f && pickedColor.v >= 0.8f -> {
            // Light
            darken()
        }

        pickedColor.s >= 0.1f && pickedColor.v <= 0.6f -> {
            // Dark
            lighten()
        }

        else -> {
            if (isDarkColor(this)) {
                lighten()
            } else {
                darken()
            }
        }
    }
}

private fun Color.lighten(): Color {
    return this.hsv(
        s = 0.3f,
        v = 1f
    )
}

private fun Color.darken(): Color {
    return this.hsv(
        s = 0.6f,
        v = 0.5f
    )
}

private fun Color.toHSVSpec(): HSVSpec {
    val hsv = FloatArray(3)
    val color: Int = this.toArgb()
    android.graphics.Color.colorToHSV(color, hsv)
    return HSVSpec(hsv[0], hsv[1], hsv[2])
}

private data class HSVSpec(
    val h: Float,
    val s: Float,
    val v: Float
)

private fun Color.hsv(
    h: Float? = null,
    s: Float,
    v: Float
): Color {
    val hsv = FloatArray(3)
    val color: Int = this.toArgb()
    android.graphics.Color.colorToHSV(color, hsv)

    if (h != null) {
        hsv[0] = h
    }

    hsv[1] = s
    hsv[2] = v

    return Color(android.graphics.Color.HSVToColor(hsv))
}

fun Int.toComposeColor() = Color(this)

package com.ivy.legacy.ui.theme.system

import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

internal val White = Color(0xFFFAFAFA)

internal val Black = Color(0xFF111114)

// Primary
internal val Ivy = Color(0xFF6B4DFF)

internal val Purple = Color(0xFF6B4DFF)

internal val Purple1 = Color(0xFFC34CFF)

internal val Purple2 = Color(0xFFFF4CFF)

internal val Blue = Color(0xFF4CC3FF)

internal val Blue2 = Color(0xFF45E6E6)

internal val Blue3 = Color(0xFF457BE6)

internal val Green = Color(0xFF14CC9E)

internal val Green2 = Color(0xFF45E67B)

internal val Green3 = Color(0xFF96E645)

internal val Green4 = Color(0xFFC7E62E)

internal val Yellow = Color(0xFFFFEE33)

internal val Orange = Color(0xFFF29F30)

internal val Orange2 = Color(0xFFE67B45)

internal val Orange3 = Color(0xFFFFC34C)

internal val Red = Color(0xFFFF4060)

internal val Red2 = Color(0xFFE62E2E)

internal val Red3 = Color(0xFFFF4CA6)

// Light
internal val IvyLight = Color(0xFFD5CCFF)

internal val Purple1Light = Color(0xFFEECCFF)

internal val Purple2Light = Color(0xFFFFBFFF)

internal val BlueLight = Color(0xFFB3E6FF)

internal val Blue2Light = Color(0xFFB3FFFF)

internal val Blue3Light = Color(0xFFCCDDFF)

internal val GreenLight = Color(0xFFAAF2E0)

internal val Green2Light = Color(0xFF99FFBB)

internal val Green3Light = Color(0xFFCCFF99)

internal val Green4Light = Color(0xFFEEFF99)

internal val YellowLight = Color(0xFFFFF799)

internal val OrangeLight = Color(0xFFFFDEB3)

internal val Orange2Light = Color(0xFFFFCCB3)

internal val Orange3Light = Color(0xFFFFDC99)

internal val RedLight = Color(0xFFFFCCD5)

internal val Red2Light = Color(0xFFFFB3B3)

internal val Red3Light = Color(0xFFFFCCE6)

// Dark
internal val IvyDark = Color(0xFF352680)

internal val Purple1Dark = Color(0xFF622680)

internal val Purple2Dark = Color(0xFF802680)

internal val BlueDark = Color(0xFF266280)

internal val Blue2Dark = Color(0xFF227373)

internal val Blue3Dark = Color(0xFF223D73)

internal val GreenDark = Color(0xFF0A664F)

internal val Green2Dark = Color(0xFF22733D)

internal val Green3Dark = Color(0xFF66804D)

internal val Green4Dark = Color(0xFF637317)

internal val YellowDark = Color(0xFF807719)

internal val OrangeDark = Color(0xFF734B17)

internal val Orange2Dark = Color(0xFF66371F)

internal val Orange3Dark = Color(0xFF806226)

internal val RedDark = Color(0xFF801919)

internal val Red2Dark = Color(0xFF802030)

internal val Red3Dark = Color(0xFF802653)
// --------------------------------------------------------------------------------------------------

internal val TrueBlack = Color(0xFF000000)

internal val MediumBlack = Color(0xFF2B2C2D)

internal val Gray = Color(0xFF939199)

internal val MediumWhite = Color(0xFFEFEEF0)

internal val Transparent = Color(0x00000000)

internal val GradientGreen = Gradient(Green, Color(0xFF49F2C8))

@Immutable
internal data class Gradient(
    val startColor: Color,
    val endColor: Color
) {
    companion object {
                fun from(startColor: Int, endColor: Int?) = Gradient(
            startColor = startColor.toComposeColor(),
            endColor = (endColor ?: startColor).toComposeColor()
        )

                fun solid(color: Color) = Gradient(color, color)

                @Composable
        fun black() = Gradient(LegacyTheme.colors.gray, LegacyTheme.colors.pureInverse)
    }

        fun asHorizontalBrush() = Brush.horizontalGradient(colors = listOf(startColor, endColor))
}

internal fun findContrastTextColor(backgroundColor: Color): Color {
    return if (isDarkColor(backgroundColor.toArgb())) White else Black
}

internal fun isDarkColor(color: Color): Boolean {
    return isDarkColor(color.toArgb())
}

internal fun isDarkColor(@ColorInt color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) <= 0.5
}

internal fun Color.asBrush(): Brush {
    return Brush.horizontalGradient(listOf(this, this))
}

internal fun Color.dynamicContrast(): Color {
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

internal fun Int.toComposeColor() = Color(this)

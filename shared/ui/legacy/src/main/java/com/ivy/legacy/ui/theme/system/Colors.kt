package com.ivy.legacy.ui.theme.system

import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val White = Color(0xFFFAFAFA)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Black = Color(0xFF111114)

// Primary
@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Ivy = Color(0xFF6B4DFF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Purple = Color(0xFF6B4DFF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Purple1 = Color(0xFFC34CFF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Purple2 = Color(0xFFFF4CFF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Blue = Color(0xFF4CC3FF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Blue2 = Color(0xFF45E6E6)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Blue3 = Color(0xFF457BE6)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Green = Color(0xFF14CC9E)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Green2 = Color(0xFF45E67B)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Green3 = Color(0xFF96E645)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Green4 = Color(0xFFC7E62E)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Yellow = Color(0xFFFFEE33)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Orange = Color(0xFFF29F30)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Orange2 = Color(0xFFE67B45)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Orange3 = Color(0xFFFFC34C)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Red = Color(0xFFFF4060)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Red2 = Color(0xFFE62E2E)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Red3 = Color(0xFFFF4CA6)

// Light
@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val IvyLight = Color(0xFFD5CCFF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Purple1Light = Color(0xFFEECCFF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Purple2Light = Color(0xFFFFBFFF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val BlueLight = Color(0xFFB3E6FF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Blue2Light = Color(0xFFB3FFFF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Blue3Light = Color(0xFFCCDDFF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val GreenLight = Color(0xFFAAF2E0)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Green2Light = Color(0xFF99FFBB)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Green3Light = Color(0xFFCCFF99)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Green4Light = Color(0xFFEEFF99)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val YellowLight = Color(0xFFFFF799)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val OrangeLight = Color(0xFFFFDEB3)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Orange2Light = Color(0xFFFFCCB3)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Orange3Light = Color(0xFFFFDC99)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val RedLight = Color(0xFFFFCCD5)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Red2Light = Color(0xFFFFB3B3)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Red3Light = Color(0xFFFFCCE6)

// Dark
@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val IvyDark = Color(0xFF352680)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Purple1Dark = Color(0xFF622680)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Purple2Dark = Color(0xFF802680)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val BlueDark = Color(0xFF266280)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Blue2Dark = Color(0xFF227373)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Blue3Dark = Color(0xFF223D73)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val GreenDark = Color(0xFF0A664F)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Green2Dark = Color(0xFF22733D)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Green3Dark = Color(0xFF66804D)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Green4Dark = Color(0xFF637317)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val YellowDark = Color(0xFF807719)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val OrangeDark = Color(0xFF734B17)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Orange2Dark = Color(0xFF66371F)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Orange3Dark = Color(0xFF806226)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val RedDark = Color(0xFF801919)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Red2Dark = Color(0xFF802030)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Red3Dark = Color(0xFF802653)
// --------------------------------------------------------------------------------------------------

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val TrueBlack = Color(0xFF000000)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val MediumBlack = Color(0xFF2B2C2D)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Gray = Color(0xFF939199)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val MediumWhite = Color(0xFFEFEEF0)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Transparent = Color(0x00000000)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val GradientGreen = Gradient(Green, Color(0xFF49F2C8))

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Immutable
data class Gradient(
    val startColor: Color,
    val endColor: Color
) {
    companion object {
        @Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
        fun from(startColor: Int, endColor: Int?) = Gradient(
            startColor = startColor.toComposeColor(),
            endColor = (endColor ?: startColor).toComposeColor()
        )

        @Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
        fun solid(color: Color) = Gradient(color, color)

        @Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
        @Composable
        fun black() = Gradient(LegacyTheme.colors.gray, LegacyTheme.colors.pureInverse)
    }

    @Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
    fun asHorizontalBrush() = Brush.horizontalGradient(colors = listOf(startColor, endColor))
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun findContrastTextColor(backgroundColor: Color): Color {
    return if (isDarkColor(backgroundColor.toArgb())) White else Black
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun isDarkColor(color: Color): Boolean {
    return isDarkColor(color.toArgb())
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun isDarkColor(@ColorInt color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) <= 0.5
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun Color.asBrush(): Brush {
    return Brush.horizontalGradient(listOf(this, this))
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
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

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun Color.lighten(): Color {
    return this.hsv(
        s = 0.3f,
        v = 1f
    )
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun Color.darken(): Color {
    return this.hsv(
        s = 0.6f,
        v = 0.5f
    )
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun Color.toHSVSpec(): HSVSpec {
    val hsv = FloatArray(3)
    val color: Int = this.toArgb()
    android.graphics.Color.colorToHSV(color, hsv)
    return HSVSpec(hsv[0], hsv[1], hsv[2])
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
data class HSVSpec(
    val h: Float,
    val s: Float,
    val v: Float
)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun Color.hsv(
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

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun Int.toComposeColor() = Color(this)

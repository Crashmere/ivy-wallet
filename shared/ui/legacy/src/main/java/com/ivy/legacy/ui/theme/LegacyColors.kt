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
import com.ivy.legacy.ui.theme.system.LegacyTheme

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val White = Color(0xFFFAFAFA)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Black = Color(0xFF111114)

// Primary
@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Ivy = Color(0xFF6B4DFF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Blue = Color(0xFF4CC3FF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Green = Color(0xFF14CC9E)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Orange = Color(0xFFF29F30)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Red = Color(0xFFFF4060)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Red3 = Color(0xFFFF4CA6)

// Light
@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val IvyLight = Color(0xFFD5CCFF)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val GreenLight = Color(0xFFAAF2E0)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val OrangeLight = Color(0xFFFFDEB3)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val RedLight = Color(0xFFFFCCD5)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Red3Light = Color(0xFFFFCCE6)

// Dark
@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val IvyDark = Color(0xFF352680)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Purple1Dark = Color(0xFF622680)

val Purple = Color(0xFFA020F0)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val GreenDark = Color(0xFF0A664F)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val OrangeDark = Color(0xFF734B17)
// --------------------------------------------------------------------------------------------------

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val MediumBlack = Color(0xFF2B2C2D)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Gray = Color(0xFF939199)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val MediumWhite = Color(0xFFEFEEF0)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val Transparent = Color(0x00000000)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val GradientRed = Gradient(Red, Color(0xFFFF99AB))

val GradientPurple = Gradient(Purple, Color(0xFFED3EF7))

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val GradientGreen = Gradient(Green, Color(0xFF49F2C8))

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val GradientOrangeRevert = Gradient(Color(0xFFF2CD9E), Orange)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
val GradientIvy = Gradient(Ivy, Color(0xFFAA99FF))

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
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

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun Modifier.gradientCutBackgroundBottom(
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

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun pureBlur() = LegacyTheme.colors.pure.copy(alpha = 0.95f)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun mediumBlur() = LegacyTheme.colors.medium.copy(alpha = 0.95f)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun gradientExpenses() = Gradient(LegacyTheme.colors.pureInverse, LegacyTheme.colors.gray)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
data class Gradient(
    val startColor: Color,
    val endColor: Color
) {
    companion object {
        @Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
        fun from(gradient: com.ivy.legacy.ui.theme.system.Gradient) =
            Gradient(gradient.startColor, gradient.endColor)

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

    @Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
    fun asVerticalBrush() = Brush.verticalGradient(colors = listOf(startColor, endColor))
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

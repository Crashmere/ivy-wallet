package com.ivy.ui.theme.colors

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

private val ContrastTextOnLight = Color(0xFF111114)

fun findContrastTextColor(backgroundColor: Color): Color {
    return if (isDarkColor(backgroundColor)) IvyFixedColors.White else ContrastTextOnLight
}

fun isDarkColor(color: Color): Boolean {
    return isDarkColor(color.toArgb())
}

fun isDarkColor(@ColorInt color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) <= 0.5
}

fun Color.dynamicContrast(): Color {
    val pickedColor = toHSVSpec()

    return when {
        pickedColor.s >= 0.5f && pickedColor.v >= 0.4f -> {
            if (isDarkColor(this)) {
                lighten()
            } else {
                darken()
            }
        }

        pickedColor.s <= 0.5f && pickedColor.v >= 0.8f -> {
            darken()
        }

        pickedColor.s >= 0.1f && pickedColor.v <= 0.6f -> {
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

fun Int.toComposeColor() = Color(this)

private fun Color.lighten(): Color {
    return hsv(
        s = 0.3f,
        v = 1f
    )
}

private fun Color.darken(): Color {
    return hsv(
        s = 0.6f,
        v = 0.5f
    )
}

private fun Color.toHSVSpec(): HSVSpec {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(toArgb(), hsv)
    return HSVSpec(hsv[0], hsv[1], hsv[2])
}

private data class HSVSpec(
    val h: Float,
    val s: Float,
    val v: Float,
)

private fun Color.hsv(
    h: Float? = null,
    s: Float,
    v: Float,
): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(toArgb(), hsv)

    if (h != null) {
        hsv[0] = h
    }

    hsv[1] = s
    hsv[2] = v

    return Color(android.graphics.Color.HSVToColor(hsv))
}

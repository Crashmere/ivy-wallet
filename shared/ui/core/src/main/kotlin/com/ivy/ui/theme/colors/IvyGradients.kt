package com.ivy.ui.theme.colors

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class Gradient(
    val startColor: Color,
    val endColor: Color,
) {
    companion object {
        fun solid(color: Color) = Gradient(color, color)
    }

    fun asHorizontalBrush() = Brush.horizontalGradient(colors = listOf(startColor, endColor))

    fun asVerticalBrush() = Brush.verticalGradient(colors = listOf(startColor, endColor))
}

object IvyGradients {
    val Green = Gradient(Color(0xFF14CC9E), Color(0xFF49F2C8))
    val Red = Gradient(Color(0xFFFF4060), Color(0xFFFF99AB))
    val Ivy = Gradient(IvyFixedColors.Ivy, Color(0xFFAA99FF))
}

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

    internal fun asVerticalBrush() = Brush.verticalGradient(colors = listOf(startColor, endColor))
}

object IvyGradients {
    val Green = Gradient(Color(0xFF14CC9E), Color(0xFF49F2C8))
    val Mint = Gradient(Color(0xFF0FB89B), Color(0xFF3FE0B0))
    val Dark = Gradient(Color(0xFF2A2C44), Color(0xFF3C3E5E))
    internal val Red = Gradient(IvyFixedColors.Red, Color(0xFFFF99AB))
    val Ivy = Gradient(IvyFixedColors.Ivy, Color(0xFFAA99FF))
    internal val OrangeRevert = Gradient(Color(0xFFF2CD9E), IvyFixedColors.Orange)
}

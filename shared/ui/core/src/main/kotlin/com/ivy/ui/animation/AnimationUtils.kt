package com.ivy.ui.animation

import android.animation.ArgbEvaluator
import androidx.annotation.FloatRange
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.roundToInt

const val DURATION_MODAL_ANIM = 200

fun <T> springBounce(
    stiffness: Float = 500f,
) = spring<T>(
    dampingRatio = 0.75f,
    stiffness = stiffness,
)

fun <T> springBounceFast() = springBounce<T>(
    stiffness = 2000f
)

fun lerp(start: Int, end: Int, @FloatRange(from = 0.0, to = 1.0) fraction: Float): Int {
    return ((start + fraction * (end - start)).roundToInt())
}

fun lerp(start: Float, end: Float, @FloatRange(from = 0.0, to = 1.0) fraction: Float): Float {
    return (start + fraction * (end - start))
}

fun lerp(start: Double, end: Double, @FloatRange(from = 0.0, to = 1.0) fraction: Double): Double {
    return (start + fraction * (end - start))
}

fun colorLerp(start: Color, end: Color, fraction: Float): Color {
    return Color(ArgbEvaluator().evaluate(fraction, start.toArgb(), end.toArgb()) as Int)
}

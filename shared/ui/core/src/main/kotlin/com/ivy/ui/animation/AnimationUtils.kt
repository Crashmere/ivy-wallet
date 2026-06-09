package com.ivy.ui.animation

import androidx.annotation.FloatRange
import androidx.compose.animation.core.spring
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

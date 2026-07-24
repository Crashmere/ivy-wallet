package com.ivy.ui.theme.colors

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Picks a visually pleasing color that is as distinct as possible from
 * [usedColors].
 *
 * Colors are generated in HSV space with saturation and value clamped to a
 * vivid-but-comfortable band, so every result reads well and keeps enough
 * contrast for an icon drawn on top. The hue is placed in the widest empty
 * slice of the hue wheel, which keeps the new color from clashing with the ones
 * already in use. A little randomness stops repeated suggestions from looking
 * mechanical.
 */
fun suggestUniqueColor(
    usedColors: List<Int>,
    random: Random = Random.Default,
): Color {
    val saturation = SATURATION_MIN + random.nextFloat() * (SATURATION_MAX - SATURATION_MIN)
    val value = VALUE_MIN + random.nextFloat() * (VALUE_MAX - VALUE_MIN)

    val usedHues = usedColors.map(::hueOf).sorted()
    val hue = if (usedHues.isEmpty()) {
        random.nextFloat() * HUE_RANGE
    } else {
        hueInWidestGap(usedHues, random)
    }

    return hsvColor(hue, saturation, value)
}

private const val HUE_RANGE = 360f
private const val SATURATION_MIN = 0.55f
private const val SATURATION_MAX = 0.80f
private const val VALUE_MIN = 0.82f
private const val VALUE_MAX = 0.98f

// Keep the pick within the middle 40% of the gap so it never drifts close to a neighbor.
private const val GAP_JITTER_FRACTION = 0.4f

private fun hueInWidestGap(sortedHues: List<Float>, random: Random): Float {
    var gapStart = 0f
    var widestGap = -1f
    for (i in sortedHues.indices) {
        val current = sortedHues[i]
        val next = if (i == sortedHues.lastIndex) sortedHues[0] + HUE_RANGE else sortedHues[i + 1]
        val gap = next - current
        if (gap > widestGap) {
            widestGap = gap
            gapStart = current
        }
    }
    val jitter = (random.nextFloat() - 0.5f) * (widestGap * GAP_JITTER_FRACTION)
    return (gapStart + widestGap / 2f + jitter).mod(HUE_RANGE)
}

private fun hueOf(argb: Int): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb, hsv)
    return hsv[0]
}

private fun hsvColor(hue: Float, saturation: Float, value: Float): Color =
    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue.mod(HUE_RANGE), saturation, value)))

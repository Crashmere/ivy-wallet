package com.ivy.data.db.migration

import android.graphics.Color
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlin.math.pow

/**
 * Deepens category colours that are too light to be legible.
 *
 * Category icons are drawn with the category colour as the icon tint on top of a light surface
 * (a faded version of the same colour over the light card background). Pale, high-brightness
 * colours — the seeded "交通" `#FFF799` being the worst offender — end up nearly invisible.
 *
 * For every distinct category colour we keep the hue but, when the colour is too bright to read
 * on a light background, boost its saturation and lower its brightness just enough to reach a
 * comfortable contrast. Colours that are already dark enough are left untouched, so a user's
 * deliberate dark picks are preserved. This is a data-only migration (schema is unchanged).
 */
internal class Migration135to136_CategoryColorContrast : Migration(135, 136) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Snapshot the distinct colours first, then apply. Because we only ever map a light
        // colour to a darker one (and never touch already-dark colours), no remapped value can
        // collide with a key we still have to process, so a plain per-colour UPDATE is safe.
        val colorMapping = HashMap<Int, Int>()
        database.query("SELECT DISTINCT color FROM categories").use { cursor ->
            val colorIndex = cursor.getColumnIndexOrThrow("color")
            while (cursor.moveToNext()) {
                val original = cursor.getInt(colorIndex)
                val improved = improveIconContrastOnLight(original)
                if (improved != original) {
                    colorMapping[original] = improved
                }
            }
        }

        colorMapping.forEach { (original, improved) ->
            database.execSQL(
                "UPDATE categories SET color = ? WHERE color = ?",
                arrayOf<Any>(improved, original)
            )
        }
    }

    private fun improveIconContrastOnLight(argb: Int): Int {
        if (relativeLuminance(argb) <= MAX_LUMINANCE) return argb

        val alpha = (argb ushr 24) and 0xFF
        val hsv = FloatArray(3)
        Color.colorToHSV(argb, hsv)
        val hue = hsv[0]
        // Near-greys have an ill-defined hue; keep them neutral and only darken.
        val saturation = if (hsv[1] < NEAR_GREY_SATURATION) hsv[1] else maxOf(hsv[1], MIN_SATURATION)
        var value = hsv[2]

        var candidate = Color.HSVToColor(alpha, floatArrayOf(hue, saturation, value))
        var guard = 0
        while (relativeLuminance(candidate) > MAX_LUMINANCE && value > MIN_VALUE && guard++ < MAX_STEPS) {
            value -= VALUE_STEP
            candidate = Color.HSVToColor(alpha, floatArrayOf(hue, saturation, value))
        }
        return candidate
    }

    private fun relativeLuminance(argb: Int): Double {
        fun channel(component: Int): Double {
            val c = component / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }

        val r = channel((argb shr 16) and 0xFF)
        val g = channel((argb shr 8) and 0xFF)
        val b = channel(argb and 0xFF)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private companion object {
        // Colours brighter than this (WCAG relative luminance, white ≈ 0.97) get deepened.
        const val MAX_LUMINANCE = 0.45
        const val MIN_SATURATION = 0.6f
        const val MIN_VALUE = 0.35f
        const val VALUE_STEP = 0.02f
        const val NEAR_GREY_SATURATION = 0.08f
        const val MAX_STEPS = 40
    }
}

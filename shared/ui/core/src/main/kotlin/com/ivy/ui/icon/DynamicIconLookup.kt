package com.ivy.ui.icon

import android.content.Context
import androidx.annotation.DrawableRes
import java.util.Locale

internal data class DynamicIconInfo(
    @DrawableRes
    val iconId: Int,
    val size: DynamicIconSize,
    val newFormat: Boolean,
)

internal enum class DynamicIconSize(
    val resourceSuffix: String,
) {
    L("l"),
    M("m"),
    S("s"),
    UNKNOWN(""),
}

internal fun getDynamicIconInfo(
    context: Context,
    iconName: String?,
    size: DynamicIconSize,
): DynamicIconInfo? {
    return iconName?.let {
        try {
            val iconNameNormalized = iconName.normalizedIconName()

            val iconId = context.resources.getIdentifier(
                "ic_custom_${iconNameNormalized}_${size.resourceSuffix}",
                "drawable",
                context.packageName,
            ).takeIf { it != 0 }

            iconId?.let { nonNullId ->
                DynamicIconInfo(
                    iconId = nonNullId,
                    size = size,
                    newFormat = false,
                )
            } ?: fallbackToNewIconFormat(
                context = context,
                iconName = iconName,
                size = size,
            )
        } catch (e: Exception) {
            fallbackToNewIconFormat(
                context = context,
                iconName = iconName,
                size = size,
            )
        }
    }
}

private fun fallbackToNewIconFormat(
    context: Context,
    iconName: String?,
    size: DynamicIconSize,
): DynamicIconInfo? {
    return iconName?.let {
        try {
            val iconId = context.resources.getIdentifier(
                iconName.normalizedIconName(),
                "drawable",
                context.packageName,
            ).takeIf { it != 0 }

            iconId?.let { nonNullId ->
                DynamicIconInfo(
                    iconId = nonNullId,
                    size = size,
                    newFormat = true,
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}

private fun String.normalizedIconName(): String =
    replace(" ", "")
        .trim()
        .lowercase(Locale.getDefault())

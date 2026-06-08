package com.ivy.ui.icon

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@DrawableRes
@Composable
fun getCustomIconIdS(
    iconName: String?,
    @DrawableRes defaultIcon: Int
): Int {
    val context = LocalContext.current
    return getCustomIconId(
        context = context,
        iconName = iconName,
        size = "s"
    ) ?: defaultIcon
}

@DrawableRes
private fun getCustomIconId(
    context: Context,
    iconName: String?,
    size: String,
): Int? {
    return iconName?.let {
        try {
            val iconNameNormalized = iconName
                .replace(" ", "")
                .trim()
                .lowercase(Locale.getDefault())

            context.resources.getIdentifier(
                "ic_custom_${iconNameNormalized}_$size",
                "drawable",
                context.packageName
            ).takeIf { it != 0 } ?: fallbackToNewIconFormat(
                context = context,
                iconName = iconName,
            )
        } catch (e: Exception) {
            fallbackToNewIconFormat(
                context = context,
                iconName = iconName,
            )
        }
    }
}

@DrawableRes
private fun fallbackToNewIconFormat(
    context: Context,
    iconName: String?,
): Int? {
    return iconName?.let {
        try {
            val iconNameNormalized = iconName
                .replace(" ", "")
                .trim()
                .lowercase(Locale.getDefault())

            context.resources.getIdentifier(
                iconNameNormalized,
                "drawable",
                context.packageName
            ).takeIf { it != 0 }
        } catch (e: Exception) {
            null
        }
    }
}

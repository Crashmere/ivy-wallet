package com.ivy.ui.icon

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@DrawableRes
@Composable
fun getCustomIconIdS(
    iconName: String?,
    @DrawableRes defaultIcon: Int
): Int {
    val context = LocalContext.current
    return getDynamicIconInfo(
        context = context,
        iconName = iconName,
        size = DynamicIconSize.S
    )?.iconId ?: defaultIcon
}

package com.ivy.ui.icon

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ItemIconMDefaultIcon(
    modifier: Modifier = Modifier,
    iconName: String?,
    tint: Color,
    @DrawableRes defaultIcon: Int,
    iconContentScale: ContentScale? = null,
) {
    ItemIconM(
        modifier = modifier,
        iconName = iconName,
        tint = tint,
        iconContentScale = iconContentScale,
        Default = {
            Image(
                modifier = modifier,
                painter = painterResource(id = defaultIcon),
                colorFilter = ColorFilter.tint(tint),
                contentDescription = "item icon",
            )
        },
    )
}

@Composable
private fun ItemIconM(
    modifier: Modifier = Modifier,
    iconName: String?,
    tint: Color,
    iconContentScale: ContentScale? = null,
    Default: (@Composable () -> Unit)? = null,
) {
    ItemIcon(
        modifier = modifier.size(48.dp),
        size = DynamicIconSize.M,
        iconName = iconName,
        tint = tint,
        iconContentScale = iconContentScale,
        Default = Default,
    )
}

@Composable
fun ItemIconSDefaultIcon(
    modifier: Modifier = Modifier,
    iconName: String?,
    tint: Color,
    @DrawableRes defaultIcon: Int,
) {
    ItemIconS(
        modifier = modifier,
        iconName = iconName,
        tint = tint,
        Default = {
            Image(
                modifier = modifier,
                painter = painterResource(id = defaultIcon),
                colorFilter = ColorFilter.tint(tint),
                contentDescription = "item icon",
            )
        },
    )
}

@Composable
fun ItemIconS(
    modifier: Modifier = Modifier,
    iconName: String?,
    tint: Color,
    iconContentScale: ContentScale? = null,
    Default: (@Composable () -> Unit)? = null,
) {
    ItemIcon(
        modifier = modifier.size(32.dp),
        size = DynamicIconSize.S,
        iconName = iconName,
        tint = tint,
        iconContentScale = iconContentScale,
        Default = Default,
    )
}

@Composable
private fun ItemIcon(
    modifier: Modifier = Modifier,
    iconName: String?,
    size: DynamicIconSize,
    tint: Color,
    iconContentScale: ContentScale? = null,
    Default: (@Composable () -> Unit)? = null,
) {
    val iconInfo = getDynamicIconInfo(
        context = LocalContext.current,
        iconName = iconName,
        size = size,
    )

    if (iconInfo != null) {
        Image(
            modifier = modifier.applyIconPadding(iconInfo),
            painter = painterResource(id = iconInfo.iconId),
            colorFilter = ColorFilter.tint(tint),
            alignment = Alignment.Center,
            contentScale = iconContentScale ?: if (iconInfo.newFormat) {
                ContentScale.Fit
            } else {
                ContentScale.None
            },
            contentDescription = iconName ?: "item icon",
        )
    } else {
        Default?.invoke()
    }
}

private fun Modifier.applyIconPadding(iconInfo: DynamicIconInfo): Modifier {
    if (!iconInfo.newFormat) {
        return this
    }

    return when (iconInfo.size) {
        DynamicIconSize.L -> padding(all = 4.dp)
        DynamicIconSize.M -> padding(all = 4.dp)
        DynamicIconSize.S -> padding(all = 4.dp)
        DynamicIconSize.UNKNOWN -> this
    }
}

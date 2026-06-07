package com.ivy.legacy.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Gradient

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun CloseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CircleButton(
        modifier = modifier,
        icon = R.drawable.ic_dismiss,
        contentDescription = "close",
        onClick = onClick,
    )
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun CircleButton(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    contentDescription: String = "icon",
    backgroundColor: Color = LegacyTheme.colors.pure,
    borderColor: Color = LegacyTheme.colors.medium,
    tint: Color? = LegacyTheme.colors.pureInverse,
    onClick: () -> Unit,
) {
    Icon(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .clickable(onClick = onClick) // enlarge click area
            .padding(6.dp),
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        tint = tint ?: Color.Unspecified,
    )
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun CircleButtonFilled(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    contentDescription: String = "icon",
    backgroundColor: Color = LegacyTheme.colors.medium,
    tint: Color? = LegacyTheme.colors.pureInverse,
    clickAreaPadding: Dp = 8.dp,
    onClick: () -> Unit,
) {
    Icon(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .clickable(onClick = onClick) // enlarge click area
            .padding(clickAreaPadding),
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        tint = tint ?: Color.Unspecified,
    )
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun CircleButtonFilledGradient(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    contentDescription: String = "icon",
    iconPadding: Dp = 8.dp,
    backgroundGradient: Gradient = Gradient.solid(LegacyTheme.colors.medium),
    tint: Color? = LegacyTheme.colors.pureInverse,
    onClick: () -> Unit,
) {
    Icon(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundGradient.asHorizontalBrush(), CircleShape)
            .clickable(onClick = onClick) // enlarge click area
            .padding(iconPadding),
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        tint = tint ?: Color.Unspecified,
    )
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CircleButton(
        modifier = modifier,
        icon = R.drawable.ic_back,
        contentDescription = "back",
        onClick = onClick,
    )
}
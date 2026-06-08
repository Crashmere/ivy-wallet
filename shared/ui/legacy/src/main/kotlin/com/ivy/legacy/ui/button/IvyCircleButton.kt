package com.ivy.legacy.ui.button

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.compose.GradientIconButton
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyGradients

@Composable
internal fun IvyCircleButton(
    modifier: Modifier = Modifier,
    backgroundPadding: Dp = 0.dp,
    backgroundGradient: Gradient = IvyGradients.Ivy,
    horizontalGradient: Boolean = true,
    @DrawableRes icon: Int,
    tint: Color = Color(0xFFFAFAFA),
    enabled: Boolean = true,
    hasShadow: Boolean = true,
    onClick: () -> Unit
) {
    GradientIconButton(
        modifier = modifier,
        backgroundPadding = backgroundPadding,
        backgroundGradient = backgroundGradient,
        horizontalGradient = horizontalGradient,
        icon = icon,
        tint = tint,
        enabled = enabled,
        hasShadow = hasShadow,
        disabledBackgroundColor = LegacyTheme.colors.gray,
        onClick = onClick
    )
}

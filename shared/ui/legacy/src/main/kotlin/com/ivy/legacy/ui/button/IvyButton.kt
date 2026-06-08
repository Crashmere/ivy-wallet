package com.ivy.legacy.ui.button

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyGradients

@Composable
fun IvyButton(
    modifier: Modifier = Modifier,
    text: String,
    backgroundGradient: Gradient = IvyGradients.Ivy,
    textStyle: TextStyle = LegacyTheme.typo.b2.copy(
        color = Color(0xFFFAFAFA),
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start
    ),
    @DrawableRes iconStart: Int? = null,
    @DrawableRes iconEnd: Int? = null,
    iconTint: Color = Color(0xFFFAFAFA),
    enabled: Boolean = true,
    shadowAlpha: Float = 0.15f,
    wrapContentMode: Boolean = true,
    hasGlow: Boolean = true,
    padding: Dp = 12.dp,
    iconEdgePadding: Dp = 12.dp,
    iconTextPadding: Dp = 4.dp,
    onClick: () -> Unit
) {
    GradientButton(
        modifier = modifier,
        text = text,
        backgroundGradient = backgroundGradient,
        disabledBackgroundColor = LegacyTheme.colors.gray,
        shape = LegacyTheme.shapes.rFull,
        textStyle = textStyle,
        iconStart = iconStart,
        iconEnd = iconEnd,
        iconTint = iconTint,
        enabled = enabled,
        shadowAlpha = shadowAlpha,
        wrapContentMode = wrapContentMode,
        hasGlow = hasGlow,
        padding = padding,
        iconEdgePadding = iconEdgePadding,
        iconTextPadding = iconTextPadding,
        onClick = onClick,
    )
}

package com.ivy.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyGradients

@Composable
fun GradientIconButton(
    modifier: Modifier = Modifier,
    backgroundPadding: Dp = 0.dp,
    backgroundGradient: Gradient = IvyGradients.Ivy,
    horizontalGradient: Boolean = true,
    @DrawableRes icon: Int,
    tint: Color = Color(0xFFFAFAFA),
    enabled: Boolean = true,
    hasShadow: Boolean = true,
    disabledBackgroundColor: Color = Color.Gray,
    contentDescription: String = "circle button",
    onClick: () -> Unit,
) {
    Icon(
        modifier = modifier
            .thenIf(enabled && hasShadow) {
                drawColoredShadow(
                    color = backgroundGradient.startColor,
                    borderRadius = 0.dp,
                    shadowRadius = 16.dp,
                    offsetX = 0.dp,
                    offsetY = 8.dp
                )
            }
            .clip(CircleShape)
            .background(
                brush = if (enabled) {
                    if (horizontalGradient) {
                        backgroundGradient.asHorizontalBrush()
                    } else {
                        backgroundGradient.asVerticalBrush()
                    }
                } else {
                    SolidColor(disabledBackgroundColor)
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick, enabled = enabled)
            .padding(all = backgroundPadding),
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        tint = tint,
    )
}

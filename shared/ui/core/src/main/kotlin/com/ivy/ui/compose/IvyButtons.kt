package com.ivy.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.ui.theme.colors.Gradient

@Composable
fun GradientButton(
    modifier: Modifier = Modifier,
    text: String,
    backgroundGradient: Gradient,
    disabledBackgroundColor: Color,
    shape: Shape,
    textStyle: TextStyle,
    @DrawableRes iconStart: Int? = null,
    @DrawableRes iconEnd: Int? = null,
    iconTint: Color,
    enabled: Boolean = true,
    shadowAlpha: Float = 0.15f,
    wrapContentMode: Boolean = true,
    hasGlow: Boolean = true,
    padding: Dp = 12.dp,
    iconEdgePadding: Dp = 12.dp,
    iconTextPadding: Dp = 4.dp,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .thenIf(enabled && hasGlow) {
                drawColoredShadow(
                    color = backgroundGradient.startColor,
                    borderRadius = 0.dp,
                    shadowRadius = 16.dp,
                    alpha = shadowAlpha,
                    offsetX = 0.dp,
                    offsetY = 8.dp,
                )
            }
            .clip(shape)
            .background(
                brush = if (enabled) {
                    backgroundGradient.asHorizontalBrush()
                } else {
                    SolidColor(disabledBackgroundColor)
                },
                shape = shape,
            )
            .clickable(onClick = onClick, enabled = enabled),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            iconStart != null -> {
                ButtonIconStart(
                    icon = iconStart,
                    tint = iconTint,
                    iconEdgePadding = iconEdgePadding,
                    iconTextPadding = iconTextPadding,
                )
            }

            iconEnd != null && !wrapContentMode -> {
                ButtonIconEnd(
                    icon = iconEnd,
                    tint = Color.Transparent,
                    iconEdgePadding = iconEdgePadding,
                    iconTextPadding = iconTextPadding,
                )
            }

            else -> {
                Spacer(modifier = Modifier.width(24.dp))
            }
        }

        if (!wrapContentMode) {
            Spacer(modifier = Modifier.weight(1f))
        }

        Text(
            modifier = Modifier.padding(vertical = padding),
            text = text,
            style = textStyle,
        )

        if (!wrapContentMode) {
            Spacer(modifier = Modifier.weight(1f))
        }

        when {
            iconStart != null && !wrapContentMode -> {
                ButtonIconStart(
                    icon = iconStart,
                    tint = Color.Transparent,
                    iconEdgePadding = iconEdgePadding,
                    iconTextPadding = iconTextPadding,
                )
            }

            iconEnd != null -> {
                ButtonIconEnd(
                    icon = iconEnd,
                    tint = iconTint,
                    iconEdgePadding = iconEdgePadding,
                    iconTextPadding = iconTextPadding,
                )
            }

            else -> {
                Spacer(modifier = Modifier.width(24.dp))
            }
        }
    }
}

@Composable
fun OutlinedPillButton(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes iconStart: Int?,
    shape: Shape,
    solidBackground: Boolean = false,
    backgroundColor: Color,
    minWidth: Dp = Dp.Unspecified,
    minHeight: Dp = Dp.Unspecified,
    iconTint: Color,
    borderColor: Color,
    textStyle: TextStyle,
    padding: Dp = 12.dp,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .defaultMinSize(minWidth, minHeight)
            .border(2.dp, borderColor, shape)
            .thenIf(solidBackground) {
                background(backgroundColor, shape)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (iconStart != null) {
            Spacer(Modifier.width(12.dp))

            ResourceIcon(
                icon = iconStart,
                tint = iconTint,
            )

            Spacer(Modifier.width(4.dp))
        } else {
            Spacer(Modifier.width(24.dp))
        }

        Text(
            modifier = Modifier.padding(vertical = padding, horizontal = 4.dp),
            text = text,
            style = textStyle,
        )

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun ButtonIconStart(
    iconEdgePadding: Dp,
    iconTextPadding: Dp,
    @DrawableRes icon: Int,
    tint: Color,
) {
    Spacer(modifier = Modifier.width(iconEdgePadding))

    ResourceIcon(
        icon = icon,
        tint = tint,
    )

    Spacer(modifier = Modifier.width(iconTextPadding))
}

@Composable
private fun ButtonIconEnd(
    iconEdgePadding: Dp,
    iconTextPadding: Dp,
    @DrawableRes icon: Int,
    tint: Color,
) {
    Spacer(modifier = Modifier.width(iconTextPadding))

    ResourceIcon(
        icon = icon,
        tint = tint,
    )

    Spacer(modifier = Modifier.width(iconEdgePadding))
}

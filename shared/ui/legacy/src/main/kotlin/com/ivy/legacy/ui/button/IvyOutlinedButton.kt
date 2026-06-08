package com.ivy.legacy.ui.button

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.compose.OutlinedPillButton

@Composable
fun IvyOutlinedButton(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes iconStart: Int?,
    solidBackground: Boolean = false,
    minWidth: Dp = Dp.Unspecified,
    minHeight: Dp = Dp.Unspecified,
    iconTint: Color = LegacyTheme.colors.pureInverse,
    borderColor: Color = LegacyTheme.colors.medium,
    textColor: Color = LegacyTheme.colors.pureInverse,
    padding: Dp = 12.dp,
    onClick: () -> Unit,
) {
    OutlinedPillButton(
        modifier = modifier,
        text = text,
        iconStart = iconStart,
        shape = LegacyTheme.shapes.rFull,
        solidBackground = solidBackground,
        backgroundColor = LegacyTheme.colors.pure,
        minWidth = minWidth,
        minHeight = minHeight,
        iconTint = iconTint,
        borderColor = borderColor,
        textStyle = LegacyTheme.typo.b2.copy(
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Start,
        ),
        padding = padding,
        onClick = onClick,
    )
}

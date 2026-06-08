package com.ivy.legacy.ui.button

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.compose.thenIf
import com.ivy.ui.compose.ResourceIcon

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
    val pure = LegacyTheme.colors.pure
    val rFull = LegacyTheme.shapes.rFull
    Row(
        modifier = modifier
            .clip(LegacyTheme.shapes.rFull)
            .clickable(
                onClick = onClick,
            )
            .defaultMinSize(minWidth, minHeight)
            .border(2.dp, borderColor, LegacyTheme.shapes.rFull)
            .thenIf(solidBackground) {
                background(pure, rFull)
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
            style = LegacyTheme.typo.b2.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Start,
            ),
        )

        Spacer(Modifier.width(24.dp))
    }
}

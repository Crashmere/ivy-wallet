package com.ivy.importdata.csvimport.flow

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.R
import com.ivy.ui.compose.drawColoredShadow
import com.ivy.ui.compose.thenIf

@Composable
internal fun ImportGradientButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color,
    backgroundGradient: Gradient,
    @DrawableRes iconStart: Int? = null,
    hasNext: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .thenIf(enabled) {
                drawColoredShadow(
                    color = backgroundGradient.startColor,
                    borderRadius = 0.dp,
                    shadowRadius = 16.dp,
                    offsetX = 0.dp,
                    offsetY = 8.dp
                )
            }
            .clip(LegacyTheme.shapes.rFull)
            .background(
                brush = if (enabled) {
                    backgroundGradient.asHorizontalBrush()
                } else {
                    SolidColor(LegacyTheme.colors.gray)
                },
                shape = LegacyTheme.shapes.rFull
            )
            .clickable(onClick = onClick, enabled = enabled),
        contentAlignment = Alignment.Center
    ) {
        if (iconStart != null) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(vertical = 8.dp)
                    .padding(start = 24.dp),
                painter = painterResource(id = iconStart),
                contentDescription = "iconStart"
            )
        }

        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = text,
            style = LegacyTheme.typo.b2.style(
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        )

        if (hasNext && enabled) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(vertical = 8.dp)
                    .padding(end = 24.dp),
                painter = painterResource(id = R.drawable.ic_next_arrow),
                contentDescription = "next"
            )
        }
    }
}

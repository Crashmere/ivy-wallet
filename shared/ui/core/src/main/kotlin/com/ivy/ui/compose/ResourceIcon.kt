package com.ivy.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@Composable
fun ResourceIcon(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    tint: Color,
    contentDescription: String = "icon",
) {
    Icon(
        modifier = modifier,
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        tint = tint,
    )
}

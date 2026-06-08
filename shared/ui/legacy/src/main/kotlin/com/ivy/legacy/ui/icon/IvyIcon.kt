package com.ivy.legacy.ui.icon

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.ivy.legacy.ui.theme.LegacyTheme

@Composable
fun IvyIcon(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    tint: Color = LegacyTheme.colors.pureInverse,
    contentDescription: String = "icon"
) {
    Icon(
        modifier = modifier,
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        tint = tint
    )
}

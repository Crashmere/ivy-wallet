package com.ivy.ui.compose

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
import com.ivy.ui.R

@Composable
fun CloseIconButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    borderColor: Color,
    tint: Color,
    padding: Dp = 6.dp,
    onClick: () -> Unit,
) {
    Icon(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(padding),
        painter = painterResource(id = R.drawable.ic_dismiss),
        contentDescription = "close",
        tint = tint,
    )
}

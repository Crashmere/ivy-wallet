package com.ivy.importdata.csvimport.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.R

@Composable
internal fun ImportBackButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Icon(
        modifier = modifier
            .clip(CircleShape)
            .background(LegacyTheme.colors.pure, CircleShape)
            .border(2.dp, LegacyTheme.colors.medium, CircleShape)
            .clickable(onClick = onClick)
            .padding(6.dp),
        painter = painterResource(id = R.drawable.ic_back),
        contentDescription = "back",
        tint = LegacyTheme.colors.pureInverse,
    )
}

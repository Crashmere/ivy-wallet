package com.ivy.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun HomeTransactionsDividerLine(
    modifier: Modifier = Modifier,
    paddingHorizontal: Dp = 24.dp,
) {
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = paddingHorizontal),
        color = HomeTheme.colors.medium,
        thickness = 2.dp
    )
}

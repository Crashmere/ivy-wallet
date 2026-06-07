package com.ivy.legacy.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ivy.legacy.design.l0_system.LegacyTheme

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun IvyDividerLine(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(LegacyTheme.colors.medium)
    )
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun IvyDividerLineRounded(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.rFull)
    )
}
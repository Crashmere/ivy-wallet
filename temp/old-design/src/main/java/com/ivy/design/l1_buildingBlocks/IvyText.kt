package com.ivy.design.l1_buildingBlocks

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun IvyText(
    modifier: Modifier = Modifier,
    text: String,
    typo: TextStyle
) {
    Text(
        modifier = modifier,
        text = text,
        style = typo,
    )
}

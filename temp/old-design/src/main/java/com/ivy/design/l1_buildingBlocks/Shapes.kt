package com.ivy.design.l1_buildingBlocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun Shape(
    modifier: Modifier = Modifier,
    size: Dp,
    shape: Shape,
    color: Color,
) {
    Spacer(
        modifier = Modifier
            .size(size)
            .background(
                color = color,
                shape = shape
            )
    )
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun ShapeOutlined(
    modifier: Modifier = Modifier,
    size: Dp,
    shape: Shape,
    borderColor: Color,
    borderWidth: Dp = 1.dp,
) {
    Spacer(
        modifier = Modifier
            .size(size)
            .border(
                color = borderColor,
                width = borderWidth,
                shape = shape
        )
    )
}

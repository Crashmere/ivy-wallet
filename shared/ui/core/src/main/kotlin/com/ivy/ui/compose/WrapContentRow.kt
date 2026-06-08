package com.ivy.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> WrapContentRow(
    modifier: Modifier = Modifier,
    items: List<T>,
    verticalMarginBetweenRows: Dp = 8.dp,
    horizontalMarginBetweenItems: Dp = 8.dp,
    itemContent: @Composable (item: T) -> Unit
) {
    if (items.isEmpty()) return

    Layout(
        modifier = modifier,
        content = {
            for (item in items) {
                itemContent(item)
            }
        }
    ) { measurables, constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(childConstraints) }
        val itemHeight = placeables.maxOfOrNull { it.height } ?: 0

        var x = 0
        var height = 0
        for (placeable in placeables) {
            if (x + placeable.width > constraints.maxWidth) {
                x = 0
                height += itemHeight + verticalMarginBetweenRows.roundToPx()
            }
            x += placeable.width + horizontalMarginBetweenItems.roundToPx()
        }
        height += itemHeight

        layout(constraints.maxWidth, height) {
            x = 0
            var y = 0
            placeables.forEach { placeable ->
                if (x + placeable.width > constraints.maxWidth) {
                    x = 0
                    y += itemHeight + verticalMarginBetweenRows.roundToPx()
                }

                placeable.place(x, y)
                x += placeable.width + horizontalMarginBetweenItems.roundToPx()
            }
        }
    }
}

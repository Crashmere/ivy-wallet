package com.ivy.legacy.ui.modal.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.system.Blue
import com.ivy.legacy.ui.theme.system.Blue2
import com.ivy.legacy.ui.theme.system.Blue2Dark
import com.ivy.legacy.ui.theme.system.Blue2Light
import com.ivy.legacy.ui.theme.system.Blue3
import com.ivy.legacy.ui.theme.system.Blue3Dark
import com.ivy.legacy.ui.theme.system.Blue3Light
import com.ivy.legacy.ui.theme.system.BlueDark
import com.ivy.legacy.ui.theme.system.BlueLight
import com.ivy.legacy.ui.theme.system.Green
import com.ivy.legacy.ui.theme.system.Green2
import com.ivy.legacy.ui.theme.system.Green2Dark
import com.ivy.legacy.ui.theme.system.Green2Light
import com.ivy.legacy.ui.theme.system.Green3
import com.ivy.legacy.ui.theme.system.Green3Dark
import com.ivy.legacy.ui.theme.system.Green3Light
import com.ivy.legacy.ui.theme.system.Green4
import com.ivy.legacy.ui.theme.system.Green4Dark
import com.ivy.legacy.ui.theme.system.Green4Light
import com.ivy.legacy.ui.theme.system.GreenDark
import com.ivy.legacy.ui.theme.system.GreenLight
import com.ivy.legacy.ui.theme.system.Ivy
import com.ivy.legacy.ui.theme.system.IvyDark
import com.ivy.legacy.ui.theme.system.IvyLight
import com.ivy.legacy.ui.theme.system.Orange
import com.ivy.legacy.ui.theme.system.Orange2
import com.ivy.legacy.ui.theme.system.Orange2Dark
import com.ivy.legacy.ui.theme.system.Orange2Light
import com.ivy.legacy.ui.theme.system.Orange3
import com.ivy.legacy.ui.theme.system.Orange3Dark
import com.ivy.legacy.ui.theme.system.Orange3Light
import com.ivy.legacy.ui.theme.system.OrangeDark
import com.ivy.legacy.ui.theme.system.OrangeLight
import com.ivy.legacy.ui.theme.system.Purple1
import com.ivy.legacy.ui.theme.system.Purple1Dark
import com.ivy.legacy.ui.theme.system.Purple1Light
import com.ivy.legacy.ui.theme.system.Purple2
import com.ivy.legacy.ui.theme.system.Purple2Dark
import com.ivy.legacy.ui.theme.system.Purple2Light
import com.ivy.legacy.ui.theme.system.Red
import com.ivy.legacy.ui.theme.system.Red2
import com.ivy.legacy.ui.theme.system.Red2Dark
import com.ivy.legacy.ui.theme.system.Red2Light
import com.ivy.legacy.ui.theme.system.Red3
import com.ivy.legacy.ui.theme.system.Red3Dark
import com.ivy.legacy.ui.theme.system.Red3Light
import com.ivy.legacy.ui.theme.system.RedDark
import com.ivy.legacy.ui.theme.system.RedLight
import com.ivy.legacy.ui.theme.system.Yellow
import com.ivy.legacy.ui.theme.system.YellowDark
import com.ivy.legacy.ui.theme.system.YellowLight
import com.ivy.ui.compose.densityScope
import com.ivy.ui.compose.thenIf
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.R
import com.ivy.ui.theme.colors.dynamicContrast
import kotlinx.coroutines.launch

private data class IvyColor(
    val color: Color,
)

private val IvyColorPickerBaseColors = listOf(
    Ivy, Purple1, Purple2, Blue, Blue2, Blue3,
    Green, Green2, Green3, Green4, Yellow,
    Orange, Orange2, Orange3, Red, Red2, Red3,
)

private val IvyColorPickerVariantColors = listOf(
    IvyLight, Purple1Light, Purple2Light, BlueLight, Blue2Light, Blue3Light,
    GreenLight, Green2Light, Green3Light, Green4Light, YellowLight,
    OrangeLight, Orange2Light, Orange3Light, RedLight, Red2Light, Red3Light,
    IvyDark, Purple1Dark, Purple2Dark, BlueDark, Blue2Dark, Blue3Dark,
    GreenDark, Green2Dark, Green3Dark, Green4Dark, YellowDark,
    OrangeDark, Orange2Dark, Orange3Dark, RedDark, Red2Dark, Red3Dark,
)

@Suppress("ParameterNaming")
@Composable
internal fun ColumnScope.IvyColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = stringResource(R.string.choose_color),
        style = LegacyTheme.typo.b2.copy(
            color = LegacyTheme.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start
        )
    )

    Spacer(Modifier.height(16.dp))

    val baseIvyColors = IvyColorPickerBaseColors
        .map {
            IvyColor(
                color = it,
            )
        }

    val variantIvyColors = IvyColorPickerVariantColors
        .map {
            IvyColor(
                color = it,
            )
        }

    val ivyColors = baseIvyColors + variantIvyColors

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    densityScope {
        onCompositionStart {
            val selectedColorIndex = ivyColors.indexOfFirst { it.color == selectedColor }
            if (selectedColorIndex != -1) {
                coroutineScope.launch {
                    listState.scrollToItem(
                        index = selectedColorIndex,
                        scrollOffset = 0
                    )
                }
            }
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        state = listState
    ) {
        items(
            count = ivyColors.size
        ) { index ->
            ColorItem(
                index = index,
                ivyColor = ivyColors[index],
                selectedColor = selectedColor,
                onSelected = {
                    onColorSelected(it.color)
                }
            )
        }
    }
}

@Composable
@Suppress("ParameterNaming")
private fun ColorItem(
    index: Int,
    ivyColor: IvyColor,
    selectedColor: Color,
    onSelected: (IvyColor) -> Unit
) {
    val color = ivyColor.color
    val selected = color == selectedColor

    if (index == 0) {
        Spacer(Modifier.width(24.dp))
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(48.dp)
            .background(color, CircleShape)
            .thenIf(selected) {
                border(width = 4.dp, color = color.dynamicContrast(), CircleShape)
            }
            .clickable(onClick = {
                onSelected(ivyColor)
            })
            .testTag("color_item_${ivyColor.color.value}"),
        contentAlignment = Alignment.Center
    ) {
    }

    Spacer(Modifier.width(if (selected) 16.dp else 24.dp))
}

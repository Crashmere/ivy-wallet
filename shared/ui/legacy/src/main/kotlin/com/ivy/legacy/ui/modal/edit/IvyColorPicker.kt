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
import com.ivy.ui.compose.densityScope
import com.ivy.ui.compose.thenIf
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.R
import com.ivy.ui.theme.colors.dynamicContrast
import kotlinx.coroutines.launch

private data class IvyColor(
    val color: Color,
)

private val Ivy = Color(0xFF6B4DFF)
private val Purple1 = Color(0xFFC34CFF)
private val Purple2 = Color(0xFFFF4CFF)
private val Blue = Color(0xFF4CC3FF)
private val Blue2 = Color(0xFF45E6E6)
private val Blue3 = Color(0xFF457BE6)
private val Green = Color(0xFF14CC9E)
private val Green2 = Color(0xFF45E67B)
private val Green3 = Color(0xFF96E645)
private val Green4 = Color(0xFFC7E62E)
private val Yellow = Color(0xFFFFEE33)
private val Orange = Color(0xFFF29F30)
private val Orange2 = Color(0xFFE67B45)
private val Orange3 = Color(0xFFFFC34C)
private val Red = Color(0xFFFF4060)
private val Red2 = Color(0xFFE62E2E)
private val Red3 = Color(0xFFFF4CA6)

private val IvyLight = Color(0xFFD5CCFF)
private val Purple1Light = Color(0xFFEECCFF)
private val Purple2Light = Color(0xFFFFBFFF)
private val BlueLight = Color(0xFFB3E6FF)
private val Blue2Light = Color(0xFFB3FFFF)
private val Blue3Light = Color(0xFFCCDDFF)
private val GreenLight = Color(0xFFAAF2E0)
private val Green2Light = Color(0xFF99FFBB)
private val Green3Light = Color(0xFFCCFF99)
private val Green4Light = Color(0xFFEEFF99)
private val YellowLight = Color(0xFFFFF799)
private val OrangeLight = Color(0xFFFFDEB3)
private val Orange2Light = Color(0xFFFFCCB3)
private val Orange3Light = Color(0xFFFFDC99)
private val RedLight = Color(0xFFFFCCD5)
private val Red2Light = Color(0xFFFFB3B3)
private val Red3Light = Color(0xFFFFCCE6)

private val IvyDark = Color(0xFF352680)
private val Purple1Dark = Color(0xFF622680)
private val Purple2Dark = Color(0xFF802680)
private val BlueDark = Color(0xFF266280)
private val Blue2Dark = Color(0xFF227373)
private val Blue3Dark = Color(0xFF223D73)
private val GreenDark = Color(0xFF0A664F)
private val Green2Dark = Color(0xFF22733D)
private val Green3Dark = Color(0xFF66804D)
private val Green4Dark = Color(0xFF637317)
private val YellowDark = Color(0xFF807719)
private val OrangeDark = Color(0xFF734B17)
private val Orange2Dark = Color(0xFF66371F)
private val Orange3Dark = Color(0xFF806226)
private val RedDark = Color(0xFF801919)
private val Red2Dark = Color(0xFF802030)
private val Red3Dark = Color(0xFF802653)

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

package com.ivy.transaction

import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.data.model.Category
import com.ivy.legacy.ui.button.IvyButton
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.findContrastTextColor
import com.ivy.legacy.ui.theme.style
import com.ivy.legacy.ui.theme.toComposeColor
import com.ivy.ui.R
import com.ivy.ui.icon.getCustomIconIdS

@Composable
internal fun Category(
    category: Category?,
    onChooseCategory: () -> Unit
) {
    if (category != null) {
        CategoryButton(category = category, onClick = onChooseCategory)
    } else {
        AddCategoryButton(
            modifier = Modifier.padding(start = 24.dp),
            iconStart = R.drawable.ic_plus,
            iconTint = LegacyTheme.colors.pureInverse,
            text = stringResource(R.string.add_category),
            onClick = onChooseCategory
        )
    }
}

@Composable
private fun CategoryButton(
    category: Category,
    onClick: () -> Unit,
) {
    val contrastColor = findContrastTextColor(category.color.value.toComposeColor())
    IvyButton(
        modifier = Modifier.padding(start = 24.dp),
        text = category.name.value,
        iconStart = getCustomIconIdS(
            iconName = category.icon?.id,
            defaultIcon = R.drawable.ic_custom_category_s
        ),
        backgroundGradient = Gradient.from(category.color.value, category.color.value),
        textStyle = LegacyTheme.typo.b2.style(
            color = contrastColor,
            fontWeight = FontWeight.Bold
        ),
        iconTint = contrastColor,
        hasGlow = false,
        iconEnd = R.drawable.ic_next_arrow,
        wrapContentMode = true,
        onClick = onClick
    )
}

@Composable
private fun AddCategoryButton(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = LegacyTheme.typo.b2.style(
        color = LegacyTheme.colors.pureInverse,
        fontWeight = FontWeight.Bold
    ),
    @DrawableRes iconStart: Int,
    iconTint: Color,
    padding: Dp = 12.dp,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(LegacyTheme.shapes.rFull)
            .border(
                width = 2.dp,
                brush = Gradient.solid(LegacyTheme.colors.mediumInverse).asHorizontalBrush(),
                shape = LegacyTheme.shapes.rFull
            )
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconStart(icon = iconStart, tint = iconTint)

        Text(
            modifier = Modifier.padding(vertical = padding),
            text = text,
            style = textStyle
        )

        Spacer(modifier = Modifier.width(24.dp))
    }
}

@Composable
private fun IconStart(
    icon: Int,
    tint: Color,
) {
    Spacer(modifier = Modifier.width(12.dp))

    Icon(
        painter = painterResource(id = icon),
        contentDescription = "icon",
        tint = tint,
    )

    Spacer(modifier = Modifier.width(4.dp))
}

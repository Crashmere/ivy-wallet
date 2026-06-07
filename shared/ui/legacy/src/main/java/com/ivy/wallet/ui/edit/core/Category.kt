package com.ivy.wallet.ui.edit.core

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.data.model.Category
import com.ivy.design.l0_system.LegacyTheme
import com.ivy.design.l0_system.style
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.component.IvyBorderButton
import com.ivy.legacy.ui.component.IvyButton
import com.ivy.legacy.ui.component.getCustomIconIdS
import com.ivy.legacy.ui.theme.findContrastTextColor
import com.ivy.legacy.ui.theme.toComposeColor

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun Category(
    category: Category?,
    onChooseCategory: () -> Unit
) {
    if (category != null) {
        CategoryButton(category = category) {
            onChooseCategory()
        }
    } else {
        IvyBorderButton(
            modifier = Modifier.padding(start = 24.dp),
            iconStart = R.drawable.ic_plus,
            iconTint = LegacyTheme.colors.pureInverse,
            text = stringResource(R.string.add_category)
        ) {
            onChooseCategory()
        }
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
        iconEnd = R.drawable.ic_onboarding_next_arrow,
        wrapContentMode = true,
        onClick = onClick
    )
}

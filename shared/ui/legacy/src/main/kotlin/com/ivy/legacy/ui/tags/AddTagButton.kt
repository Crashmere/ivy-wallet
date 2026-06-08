package com.ivy.legacy.ui.tags

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.TagId
import com.ivy.legacy.ui.selection.IvyBorderButton
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.Orange3
import com.ivy.legacy.ui.theme.findContrastTextColor
import com.ivy.ui.R
import com.ivy.ui.compose.GradientButton
import kotlinx.collections.immutable.ImmutableList

@Composable
fun AddTagButton(
    transactionAssociatedTags: ImmutableList<TagId>,
    onClick: () -> Unit
) {
    if (transactionAssociatedTags.isNotEmpty()) {
        ViewTagsButton(transactionTags = transactionAssociatedTags, onClick = onClick)
    } else {
        AddTagsButton(onClick = onClick)
    }
}

@Composable
private fun ViewTagsButton(
    transactionTags: ImmutableList<TagId>,
    onClick: () -> Unit,
) {
    val contrastColor = findContrastTextColor(Orange3)
    GradientButton(
        modifier = Modifier.padding(start = 24.dp),
        text = if (transactionTags.size <= 1) "${transactionTags.size}\t Tag" else "${transactionTags.size}\t Tags",
        backgroundGradient = Gradient.solid(Orange3),
        disabledBackgroundColor = LegacyTheme.colors.gray,
        shape = LegacyTheme.shapes.rFull,
        textStyle = LegacyTheme.typo.b2.copy(
            color = contrastColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        iconTint = contrastColor,
        hasGlow = false,
        iconEnd = R.drawable.ic_next_arrow,
        wrapContentMode = true,
        onClick = onClick
    )
}

@Composable
private fun AddTagsButton(
    onClick: () -> Unit,
) {
    IvyBorderButton(
        modifier = Modifier.padding(start = 24.dp),
        iconStart = R.drawable.ic_plus,
        iconTint = LegacyTheme.colors.pureInverse,
        text = stringResource(R.string.add_tags),
        onClick = onClick
    )
}

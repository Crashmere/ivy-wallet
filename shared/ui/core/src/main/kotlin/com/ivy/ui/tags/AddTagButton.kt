package com.ivy.ui.tags

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.TagId
import com.ivy.ui.R
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.compose.OutlinedPillButton
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.findContrastTextColor

@Composable
fun AddTagButton(
    transactionAssociatedTags: List<TagId>,
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
    transactionTags: List<TagId>,
    onClick: () -> Unit,
) {
    val tagTheme = TagButtonTheme
    val selectedTagColor = tagTheme.colors.selectedTag
    val contrastColor = findContrastTextColor(selectedTagColor)
    GradientButton(
        modifier = Modifier.padding(start = 24.dp),
        text = if (transactionTags.size <= 1) "${transactionTags.size}\t Tag" else "${transactionTags.size}\t Tags",
        backgroundGradient = Gradient.solid(selectedTagColor),
        disabledBackgroundColor = tagTheme.colors.gray,
        shape = tagTheme.shapes.rFull,
        textStyle = tagTheme.typo.b2.copy(
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
    val tagTheme = TagButtonTheme
    OutlinedPillButton(
        modifier = Modifier.padding(start = 24.dp),
        iconStart = R.drawable.ic_plus,
        shape = tagTheme.shapes.rFull,
        backgroundColor = tagTheme.colors.pure,
        iconTint = tagTheme.colors.pureInverse,
        borderColor = tagTheme.colors.mediumInverse,
        text = stringResource(R.string.add_tags),
        textStyle = tagTheme.typo.b2.copy(
            color = tagTheme.colors.pureInverse,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        onClick = onClick
    )
}

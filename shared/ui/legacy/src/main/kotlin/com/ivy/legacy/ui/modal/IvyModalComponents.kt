package com.ivy.legacy.ui.modal

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.GradientGreen
import com.ivy.legacy.ui.theme.GradientRed
import com.ivy.legacy.ui.theme.White
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.compose.GradientIconButton
import com.ivy.ui.compose.OutlinedPillButton
import com.ivy.ui.modal.ModalAdd
import com.ivy.ui.modal.ModalSave

@Composable
internal fun ModalDynamicPrimaryAction(
    initialEmpty: Boolean,
    initialChanged: Boolean,

    testTagSave: String = "tag_save",
    testTagDelete: String = "tag_delete",

    onDelete: () -> Unit,
    dismiss: () -> Unit,
    onSave: () -> Unit
) {
    when {
        initialEmpty -> {
            ModalAdd(
                testTag = testTagSave
            ) {
                onSave()
                dismiss()
            }
        }
        else -> {
            if (!initialChanged) {
                ModalDelete(
                    testTag = testTagDelete
                ) {
                    onDelete()
                    dismiss()
                }
            } else {
                ModalSave(
                    modifier = Modifier.testTag(testTagSave)
                ) {
                    onSave()
                    dismiss()
                }
            }
        }
    }
}

@Composable
internal fun ModalSet(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.set),
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ModalCheck(
        modifier = modifier,
        label = label,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
internal fun ModalCheck(
    modifier: Modifier = Modifier,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ModalPositiveButton(
        modifier = modifier,
        text = label,
        iconStart = R.drawable.ic_check,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
internal fun <T> ModalAddSave(
    item: T,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    if (item != null) {
        ModalSave(
            enabled = enabled,
            onClick = onClick
        )
    } else {
        ModalAdd(
            enabled = enabled,
            onClick = onClick
        )
    }
}

@Composable
internal fun ModalNegativeButton(
    text: String,
    @DrawableRes iconStart: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    GradientButton(
        text = text,
        backgroundGradient = GradientRed,
        disabledBackgroundColor = LegacyTheme.colors.gray,
        shape = LegacyTheme.shapes.rFull,
        textStyle = LegacyTheme.typo.b2.copy(
            color = White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        iconStart = iconStart,
        iconTint = White,
        onClick = onClick,
        enabled = enabled
    )
}

@Composable
internal fun ModalPositiveButton(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes iconStart: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    GradientButton(
        modifier = modifier,
        text = text,
        backgroundGradient = GradientGreen,
        disabledBackgroundColor = LegacyTheme.colors.gray,
        shape = LegacyTheme.shapes.rFull,
        textStyle = LegacyTheme.typo.b2.copy(
            color = White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        iconStart = iconStart,
        iconTint = White,
        onClick = onClick,
        enabled = enabled
    )
}

@Composable
internal fun ModalDelete(
    enabled: Boolean = true,
    testTag: String = "modal_delete",
    onClick: () -> Unit
) {
    GradientIconButton(
        modifier = Modifier
            .size(40.dp)
            .testTag(testTag),
        icon = R.drawable.ic_delete,
        backgroundGradient = GradientRed,
        enabled = enabled,
        disabledBackgroundColor = LegacyTheme.colors.gray,
        tint = White,
        onClick = onClick
    )
}

@Composable
internal fun ModalSkip(
    text: String = stringResource(R.string.skip),
    onClick: () -> Unit
) {
    OutlinedPillButton(
        text = text,
        iconStart = null,
        shape = LegacyTheme.shapes.rFull,
        solidBackground = true,
        backgroundColor = LegacyTheme.colors.pure,
        iconTint = LegacyTheme.colors.pureInverse,
        borderColor = LegacyTheme.colors.medium,
        textStyle = LegacyTheme.typo.b2.copy(
            fontWeight = FontWeight.Bold,
            color = LegacyTheme.colors.pureInverse,
            textAlign = TextAlign.Start,
        ),
    ) {
        onClick()
    }
}

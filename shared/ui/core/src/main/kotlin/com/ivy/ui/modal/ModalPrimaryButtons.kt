package com.ivy.ui.modal

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.ui.R
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.compose.GradientIconButton
import com.ivy.ui.compose.OutlinedPillButton
import com.ivy.ui.theme.colors.IvyGradients

@Composable
fun ModalSave(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ModalPrimaryButton(
        modifier = modifier,
        text = stringResource(R.string.save),
        iconStart = R.drawable.ic_save,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun ModalAdd(
    enabled: Boolean = true,
    testTag: String = "modal_add",
    onClick: () -> Unit
) {
    ModalPrimaryButton(
        modifier = Modifier.testTag(testTag),
        text = stringResource(R.string.add),
        iconStart = R.drawable.ic_plus,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun ModalSet(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.set),
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ModalPrimaryButton(
        modifier = modifier,
        text = label,
        iconStart = R.drawable.ic_check,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun ModalPositiveButton(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes iconStart: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    ModalPrimaryButton(
        modifier = modifier,
        text = text,
        iconStart = iconStart,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun ModalDelete(
    enabled: Boolean = true,
    testTag: String = "modal_delete",
    onClick: () -> Unit
) {
    val buttonTheme = ModalPrimaryButtonTheme
    GradientIconButton(
        modifier = Modifier
            .size(40.dp)
            .testTag(testTag),
        icon = R.drawable.ic_delete,
        backgroundGradient = IvyGradients.Red,
        enabled = enabled,
        disabledBackgroundColor = buttonTheme.colors.gray,
        tint = buttonTheme.colors.white,
        onClick = onClick
    )
}

@Composable
fun ModalSkip(
    text: String = stringResource(R.string.skip),
    onClick: () -> Unit
) {
    val modalTheme = IvyModalTheme
    val buttonTheme = ModalPrimaryButtonTheme
    OutlinedPillButton(
        text = text,
        iconStart = null,
        shape = buttonTheme.shapes.rFull,
        solidBackground = true,
        backgroundColor = modalTheme.colors.pure,
        iconTint = modalTheme.colors.pureInverse,
        borderColor = modalTheme.colors.medium,
        textStyle = buttonTheme.typo.b2.copy(
            fontWeight = FontWeight.Bold,
            color = modalTheme.colors.pureInverse,
            textAlign = TextAlign.Start,
        ),
        onClick = onClick
    )
}

@Composable
private fun ModalPrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes iconStart: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val buttonTheme = ModalPrimaryButtonTheme
    GradientButton(
        modifier = modifier,
        text = text,
        backgroundGradient = IvyGradients.Green,
        disabledBackgroundColor = buttonTheme.colors.gray,
        shape = buttonTheme.shapes.rFull,
        textStyle = buttonTheme.typo.b2.copy(
            color = buttonTheme.colors.white,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        iconStart = iconStart,
        iconTint = buttonTheme.colors.white,
        onClick = onClick,
        enabled = enabled
    )
}

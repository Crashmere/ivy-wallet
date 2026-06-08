package com.ivy.ui.modal

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.ivy.ui.R
import com.ivy.ui.compose.GradientButton
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

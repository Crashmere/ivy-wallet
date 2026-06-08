package com.ivy.planned.edit

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.button.IvyButton
import com.ivy.legacy.ui.button.IvyCircleButton
import com.ivy.legacy.ui.modal.IvyModal
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.GradientGreen
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.R
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.platform.hideKeyboard
import java.util.UUID

@Suppress("ParameterNaming")
@Composable
internal fun BoxWithConstraintsScope.DescriptionModal(
    id: UUID = UUID.randomUUID(),
    visible: Boolean,
    description: String?,
    onDescriptionChanged: (String?) -> Unit,
    dismiss: () -> Unit,
) {
    var descTextFieldValue by remember(description) {
        mutableStateOf(selectEndTextFieldValue(description))
    }
    val view = LocalView.current

    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            DescriptionModalPrimaryAction(
                initialEmpty = description.isNullOrBlank(),
                initialChanged = description != descTextFieldValue.text,
                onSave = {
                    onDescriptionChanged(descTextFieldValue.text)
                    view.hideKeyboard()
                },
                onDelete = {
                    onDescriptionChanged(null)
                    view.hideKeyboard()
                },
                dismiss = dismiss
            )
        }
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            modifier = Modifier.padding(start = 32.dp),
            text = stringResource(R.string.description),
            style = LegacyTheme.typo.b1.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(24.dp))

        val focus = FocusRequester()
        onCompositionStart {
            focus.requestFocus()
        }

        DescriptionTextField(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .focusRequester(focus),
            testTag = "modal_desc_input",
            value = descTextFieldValue,
            hint = stringResource(R.string.description_text_field_hint),
            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            keyboardActions = KeyboardActions(
                onAny = {
                    descTextFieldValue = descTextFieldValue.copy(
                        text = StringBuilder(descTextFieldValue.text)
                            .insert(descTextFieldValue.selection.end, "\n")
                            .toString(),
                        selection = TextRange(descTextFieldValue.selection.end + 1)
                    )
                }
            )
        ) {
            descTextFieldValue = it
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clickableNoIndication(rememberInteractionSource()) {
                    focus.requestFocus()
                }
        )
    }
}

@Composable
private fun DescriptionModalPrimaryAction(
    initialEmpty: Boolean,
    initialChanged: Boolean,
    onDelete: () -> Unit,
    dismiss: () -> Unit,
    onSave: () -> Unit
) {
    when {
        initialEmpty -> DescriptionModalPositiveButton(
            testTag = "modal_desc_save",
            text = stringResource(R.string.add),
            iconStart = R.drawable.ic_plus,
        ) {
            onSave()
            dismiss()
        }

        !initialChanged -> DescriptionModalDeleteButton(
            testTag = "modal_desc_delete"
        ) {
            onDelete()
            dismiss()
        }

        else -> DescriptionModalPositiveButton(
            modifier = Modifier.testTag("modal_desc_save"),
            text = stringResource(R.string.save),
            iconStart = R.drawable.ic_save
        ) {
            onSave()
            dismiss()
        }
    }
}

@Composable
private fun DescriptionModalPositiveButton(
    modifier: Modifier = Modifier,
    testTag: String? = null,
    text: String,
    @DrawableRes iconStart: Int,
    onClick: () -> Unit,
) {
    IvyButton(
        modifier = if (testTag != null) modifier.testTag(testTag) else modifier,
        text = text,
        backgroundGradient = GradientGreen,
        iconStart = iconStart,
        onClick = onClick
    )
}

@Composable
private fun DescriptionModalDeleteButton(
    testTag: String,
    onClick: () -> Unit
) {
    IvyCircleButton(
        modifier = Modifier
            .size(40.dp)
            .testTag(testTag),
        icon = R.drawable.ic_delete,
        backgroundGradient = Gradient(LegacyTheme.colors.red, Color(0xFFFF99AB)),
        tint = White,
        onClick = onClick
    )
}

@Composable
private fun DescriptionTextField(
    modifier: Modifier = Modifier,
    testTag: String,
    value: TextFieldValue,
    hint: String,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    onValueChanged: (TextFieldValue) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart
    ) {
        if (value.text.isBlank()) {
            Text(
                text = hint,
                textAlign = TextAlign.Start,
                style = LegacyTheme.typo.b2.style(
                    color = LegacyTheme.colors.mediumInverse,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start
                )
            )
        }

        BasicTextField(
            modifier = Modifier.testTag(testTag),
            value = value,
            onValueChange = onValueChanged,
            textStyle = LegacyTheme.typo.nB2.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            ),
            singleLine = false,
            cursorBrush = SolidColor(LegacyTheme.colors.pureInverse),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}

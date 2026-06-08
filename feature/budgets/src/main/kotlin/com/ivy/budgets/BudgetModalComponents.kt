package com.ivy.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.button.IvyCircleButton
import com.ivy.legacy.ui.modal.ModalAdd
import com.ivy.legacy.ui.modal.ModalSave
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.White
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.R
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.platform.hideKeyboard

@Composable
internal fun BudgetModalAddSave(
    isEdit: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    if (isEdit) {
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
internal fun BudgetModalDelete(
    onClick: () -> Unit,
) {
    IvyCircleButton(
        modifier = Modifier
            .size(40.dp)
            .testTag("modal_delete"),
        icon = R.drawable.ic_delete,
        backgroundGradient = Gradient.solid(LegacyTheme.colors.red),
        tint = White,
        onClick = onClick
    )
}

@Composable
internal fun BudgetNameInput(
    hint: String,
    autoFocusKeyboard: Boolean,
    textFieldValue: TextFieldValue,
    setTextFieldValue: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nameFocus = FocusRequester()

    onCompositionStart {
        if (autoFocusKeyboard) {
            nameFocus.requestFocus()
        }
    }

    val view = LocalView.current
    Column {
        val isEmpty = textFieldValue.text.isBlank()

        Box(
            modifier = modifier
                .padding(start = 32.dp, end = 36.dp)
                .focusRequester(nameFocus),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isEmpty && hint.isBlank().not()) {
                androidx.compose.material3.Text(
                    text = hint,
                    style = LegacyTheme.typo.b2.style(
                        color = LegacyTheme.colors.gray,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start
                    ),
                )
            }

            BasicTextField(
                modifier = Modifier
                    .testTag("base_input")
                    .focusRequester(nameFocus),
                value = textFieldValue,
                onValueChange = setTextFieldValue,
                textStyle = LegacyTheme.typo.b1.style(
                    color = LegacyTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                ),
                singleLine = false,
                cursorBrush = SolidColor(LegacyTheme.colors.pureInverse),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text,
                    autoCorrect = true
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        view.hideKeyboard()
                    }
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        Spacer(
            modifier = Modifier
                .padding(start = 32.dp, end = 32.dp)
                .fillMaxWidth()
                .height(2.dp)
                .background(LegacyTheme.colors.medium, LegacyTheme.shapes.rFull),
        )
    }
}

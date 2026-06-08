package com.ivy.transactions

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.button.IvyButton
import com.ivy.legacy.ui.modal.IvyModal
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.Red
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.R
import com.ivy.ui.platform.hideKeyboard
import java.util.UUID

@SuppressLint("ComposeModifierMissing")
@Composable
internal fun BoxWithConstraintsScope.TransactionsDeleteConfirmationModal(
    title: String,
    description: String,
    visible: Boolean,
    enableDeletionButton: Boolean,
    onAccountNameChange: (String) -> Unit,
    dismiss: () -> Unit,
    id: UUID = UUID.randomUUID(),
    hint: String = stringResource(id = R.string.account_name),
    buttonText: String = stringResource(R.string.delete),
    iconStart: Int = R.drawable.ic_delete,
    onDelete: () -> Unit,
) {
    var deletionTextFieldValue by remember(this) {
        mutableStateOf(TextFieldValue(""))
    }
    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            IvyButton(
                text = buttonText,
                backgroundGradient = Gradient.solid(Red),
                iconStart = iconStart,
                enabled = enableDeletionButton,
                onClick = onDelete
            )
        }
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = title,
            style = LegacyTheme.typo.b1.style(
                color = Red,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(24.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = description,
            style = LegacyTheme.typo.b2.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(Modifier.height(12.dp))

        val view = LocalView.current

        ConfirmationNameTextField(
            modifier = Modifier.padding(start = 28.dp, end = 36.dp),
            underlineModifier = Modifier.padding(start = 24.dp, end = 32.dp),
            value = deletionTextFieldValue,
            hint = hint,
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
            ),
        ) { newValue ->
            deletionTextFieldValue = newValue
            onAccountNameChange(newValue.text)
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun ConfirmationNameTextField(
    modifier: Modifier = Modifier,
    underlineModifier: Modifier = Modifier,
    value: TextFieldValue,
    hint: String?,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    onValueChanged: (TextFieldValue) -> Unit,
) {
    Column {
        val isEmpty = value.text.isBlank()

        Box(
            modifier = modifier,
            contentAlignment = Alignment.CenterStart
        ) {
            if (isEmpty && hint.isNullOrBlank().not()) {
                Text(
                    text = hint.orEmpty(),
                    style = LegacyTheme.typo.b2.style(
                        color = LegacyTheme.colors.gray,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start
                    ),
                )
            }

            BasicTextField(
                modifier = Modifier.testTag("base_input"),
                value = value,
                onValueChange = onValueChanged,
                textStyle = LegacyTheme.typo.b1.style(
                    color = LegacyTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                ),
                singleLine = false,
                cursorBrush = SolidColor(LegacyTheme.colors.pureInverse),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions
            )
        }

        Spacer(Modifier.height(8.dp))

        Spacer(
            modifier = underlineModifier
                .fillMaxWidth()
                .height(2.dp)
                .background(LegacyTheme.colors.medium, LegacyTheme.shapes.rFull),
        )
    }
}

package com.ivy.legacy.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.theme.system.style
import com.ivy.legacy.ui.hideKeyboard
import com.ivy.base.text.isNotNullOrBlank

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Suppress("ParameterNaming")
@Composable
fun IvyNumberTextField(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    value: TextFieldValue,
    hint: String?,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    textColor: Color = LegacyTheme.colors.pureInverse,
    hintColor: Color = Color.Gray,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions? = null,
    keyboardActions: KeyboardActions? = null,
    onValueChanged: (TextFieldValue) -> Unit
) {
    val isEmpty = value.text.isBlank()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isEmpty && hint.isNotNullOrBlank()) {
            Text(
                modifier = textModifier,
                text = hint!!,
                textAlign = TextAlign.Start,
                style = LegacyTheme.typo.nB2.style(
                    color = hintColor,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center
                )
            )
        }

        val view = LocalView.current
        BasicTextField(
            modifier = textModifier
                .testTag("base_number_input"),
            value = value,
            onValueChange = onValueChanged,
            textStyle = LegacyTheme.typo.nB2.style(
                color = textColor,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            cursorBrush = SolidColor(LegacyTheme.colors.pureInverse),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions ?: KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number,
                autoCorrect = false
            ),
            keyboardActions = keyboardActions ?: KeyboardActions(
                onDone = {
                    hideKeyboard(view)
                }
            )
        )
    }
}
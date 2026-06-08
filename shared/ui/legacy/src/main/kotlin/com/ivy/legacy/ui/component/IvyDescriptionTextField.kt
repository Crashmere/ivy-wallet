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
import com.ivy.ui.platform.hideKeyboard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Suppress("ParameterNaming")
@Composable
internal fun IvyDescriptionTextField(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    testTag: String = "desc_input",
    value: TextFieldValue,
    hint: String?,
    fontWeight: FontWeight = FontWeight.Medium,
    textColor: Color = LegacyTheme.colors.pureInverse,
    hintColor: Color = LegacyTheme.colors.mediumInverse,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions? = KeyboardOptions.Default,
    keyboardActions: KeyboardActions? = KeyboardActions.Default,
    onValueChanged: (TextFieldValue) -> Unit
) {
    val isEmpty = value.text.isBlank()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart
    ) {
        if (isEmpty && hint.isNullOrBlank().not()) {
            Text(
                modifier = textModifier,
                text = hint!!,
                textAlign = TextAlign.Start,
                style = LegacyTheme.typo.b2.style(
                    color = hintColor,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Start
                )
            )
        }

        val view = LocalView.current
        BasicTextField(
            modifier = textModifier.testTag(testTag),
            value = value,
            onValueChange = onValueChanged,
            textStyle = LegacyTheme.typo.nB2.style(
                color = textColor,
                fontWeight = fontWeight,
                textAlign = TextAlign.Start
            ),
            singleLine = false,
            cursorBrush = SolidColor(LegacyTheme.colors.pureInverse),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions ?: KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrect = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = keyboardActions ?: KeyboardActions(
                onDone = {
                    view.hideKeyboard()
                }
            )
        )
    }
}

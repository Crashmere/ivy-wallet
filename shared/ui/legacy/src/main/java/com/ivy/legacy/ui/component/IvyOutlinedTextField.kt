package com.ivy.legacy.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.LegacyTheme
import com.ivy.design.l0_system.style
import com.ivy.base.legacy.isNotNullOrBlank
import com.ivy.design.utils.thenIf

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Suppress("ParameterNaming")
@Composable
fun IvyOutlinedTextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    hint: String?,
    hintColor: Color = LegacyTheme.colors.gray,
    backgroundColor: Color = LegacyTheme.colors.primary,
    emptyBorderColor: Color = LegacyTheme.colors.gray,
    textColor: Color = LegacyTheme.colors.pureInverse,
    cursorColor: Color = LegacyTheme.colors.pureInverse,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    validateInput: (TextFieldValue) -> Boolean = { it.text.isNotNullOrBlank() },
    onValueChanged: (TextFieldValue) -> Unit
) {
    val isEmpty = value.text.isBlank()
    val rFull = LegacyTheme.shapes.rFull
    Box(
        modifier = modifier
            .clip(LegacyTheme.shapes.rFull)
            .border(
                width = 2.dp,
                color = if (isEmpty) emptyBorderColor else backgroundColor,
                shape = LegacyTheme.shapes.rFull
            )
            .thenIf(validateInput(value)) {
                background(backgroundColor.copy(alpha = 0.1f), rFull)
            },
        contentAlignment = Alignment.Center
    ) {
        val inputFieldFocus = FocusRequester()

        if (isEmpty && hint.isNotNullOrBlank()) {
            Text(
                modifier = Modifier
                    .clickable {
                        inputFieldFocus.requestFocus()
                    }
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                text = hint!!,
                textAlign = TextAlign.Center,
                style = LegacyTheme.typo.b2.style(
                    color = hintColor,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        BasicTextField(
            modifier = Modifier
                .focusRequester(inputFieldFocus)
                .clickable {
                    inputFieldFocus.requestFocus()
                }
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 24.dp),
            value = value,
            onValueChange = onValueChanged,
            textStyle = LegacyTheme.typo.b2.style(
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            cursorBrush = SolidColor(cursorColor),
            visualTransformation = visualTransformation,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions
        )
    }
}
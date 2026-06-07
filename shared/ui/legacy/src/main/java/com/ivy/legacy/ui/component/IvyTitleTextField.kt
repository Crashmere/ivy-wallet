package com.ivy.legacy.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.theme.system.style
import com.ivy.legacy.ui.hideKeyboard
import com.ivy.base.text.isNotNullOrBlank
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding

@Suppress("ParameterNaming")
@Composable
fun ColumnScope.IvyTitleTextField(
    modifier: Modifier = Modifier,
    dividerModifier: Modifier = Modifier,
    value: TextFieldValue,
    textColor: Color = LegacyTheme.colors.pureInverse,
    hint: String?,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        autoCorrect = true,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Done,
        capitalization = KeyboardCapitalization.Sentences
    ),
    keyboardActions: KeyboardActions? = null,
    onValueChanged: (TextFieldValue) -> Unit
) {
    val isEmpty = value.text.isBlank()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        if (isEmpty && hint.isNotNullOrBlank()) {
            Text(
                modifier = Modifier,
                text = hint!!,
                style = LegacyTheme.typo.h2.style(
                    color = LegacyTheme.colors.gray,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Start
                ),
            )
        }

        val view = LocalView.current
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_field"),
            value = value,
            onValueChange = onValueChanged,
            textStyle = LegacyTheme.typo.h2.style(
                color = textColor,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Start
            ),
            singleLine = false,
            cursorBrush = SolidColor(LegacyTheme.colors.pureInverse),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions ?: KeyboardActions(
                onDone = {
                    hideKeyboard(view)
                }
            )
        )
    }

    Spacer(Modifier.height(8.dp))

    Spacer(
        modifier = dividerModifier
            .fillMaxWidth()
            .height(2.dp)
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.rFull),
    )
}
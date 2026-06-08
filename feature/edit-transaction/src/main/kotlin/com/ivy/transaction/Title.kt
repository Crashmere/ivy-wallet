package com.ivy.transaction

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.TransactionType
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.R
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.platform.hideKeyboard
import com.ivy.ui.platform.keyboardVisibleState
import kotlinx.coroutines.launch
import java.util.UUID

private const val SUGGESTIONS_LIMIT = 10

@Suppress("ParameterNaming")
@Composable
internal fun ColumnScope.Title(
    type: TransactionType,
    titleFocus: FocusRequester,
    initialTransactionId: UUID?,
    titleTextFieldValue: TextFieldValue,
    setTitleTextFieldValue: (TextFieldValue) -> Unit,
    suggestions: Set<String>,
    onTitleChanged: (String?) -> Unit,
    scrollState: ScrollState? = null,
    onNext: () -> Unit,
) {
    TitleTextField(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .focusRequester(titleFocus),
        dividerModifier = Modifier.padding(horizontal = 24.dp),
        value = titleTextFieldValue,
        hint = when (type) {
            TransactionType.INCOME -> stringResource(R.string.income_title)
            TransactionType.EXPENSE -> stringResource(R.string.expense_title)
            TransactionType.TRANSFER -> stringResource(R.string.transfer_title)
        },
        keyboardOptions = KeyboardOptions(
            autoCorrect = true,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            capitalization = KeyboardCapitalization.Sentences
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                onNext()
            }
        )
    ) {
        setTitleTextFieldValue(it)
        onTitleChanged(it.text)
    }

    val coroutineScope = rememberCoroutineScope()
    Suggestions(suggestions = suggestions) { suggestion ->
        setTitleTextFieldValue(selectEndTextFieldValue(suggestion))
        onTitleChanged(suggestion)

        coroutineScope.launch {
            scrollState?.animateScrollTo(0)
        }
    }
}

@Suppress("ParameterNaming")
@Composable
private fun ColumnScope.TitleTextField(
    modifier: Modifier = Modifier,
    dividerModifier: Modifier = Modifier,
    value: TextFieldValue,
    textColor: Color = LegacyTheme.colors.pureInverse,
    hint: String?,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions? = null,
    onValueChanged: (TextFieldValue) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.text.isBlank() && !hint.isNullOrBlank()) {
            Text(
                text = hint,
                style = LegacyTheme.typo.h2.copy(
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
            textStyle = LegacyTheme.typo.h2.copy(
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
                    view.hideKeyboard()
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

@Composable
private fun Suggestions(
    suggestions: Set<String>,
    onClick: (String) -> Unit
) {
    val keyboardVisible by keyboardVisibleState()
    if (keyboardVisible && suggestions.isNotEmpty()) {
        for (suggestion in suggestions.take(SUGGESTIONS_LIMIT)) {
            Suggestion(suggestion = suggestion) {
                onClick(suggestion)
            }
        }
    }
}

@Composable
private fun Suggestion(
    suggestion: String,
    onClick: () -> Unit
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp)
            .padding(vertical = 12.dp),
        text = suggestion,
        style = LegacyTheme.typo.b2.copy(
            color = LegacyTheme.colors.pureInverse,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start
        )
    )
}

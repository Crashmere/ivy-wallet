package com.ivy.legacy.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.Gray
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.R
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.platform.hideKeyboard

@Suppress("MagicNumber")
@Composable
fun SearchInput(
    searchQueryTextFieldValue: TextFieldValue,
    hint: String,
    focus: Boolean = true,
    showClearIcon: Boolean = true,
    onSetSearchQueryTextField: (TextFieldValue) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(LegacyTheme.shapes.rFull)
            .background(LegacyTheme.colors.pure)
            .border(1.dp, Gray, LegacyTheme.shapes.rFull),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchIcon(icon = R.drawable.ic_search, modifier = Modifier.weight(1f))

        val searchFocus = FocusRequester()
        IvyBasicTextField(
            modifier = Modifier
                .weight(5f)
                .padding(vertical = 12.dp)
                .focusRequester(searchFocus),
            value = searchQueryTextFieldValue,
            hint = hint,
            onValueChanged = {
                onSetSearchQueryTextField(it)
            }
        )

        if (focus) {
            onCompositionStart {
                searchFocus.requestFocus()
            }
        }

        if (showClearIcon) {
            SearchIcon(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSetSearchQueryTextField(selectEndTextFieldValue(""))
                    },
                icon = R.drawable.ic_outline_clear_24
            )
        }
    }
}

@Composable
private fun SearchIcon(
    modifier: Modifier = Modifier,
    icon: Int,
) {
    Icon(
        modifier = modifier,
        painter = painterResource(id = icon),
        contentDescription = "icon",
        tint = LegacyTheme.colors.pureInverse
    )
}

@Composable
private fun IvyBasicTextField(
    modifier: Modifier = Modifier,
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
        if (isEmpty && hint.isNullOrBlank().not()) {
            Text(
                text = hint!!,
                style = LegacyTheme.typo.b2.style(
                    color = LegacyTheme.colors.gray,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                ),
            )
        }

        val view = LocalView.current
        BasicTextField(
            modifier = Modifier
                .testTag("base_input"),
            value = value,
            onValueChange = onValueChanged,
            textStyle = LegacyTheme.typo.b2.style(
                fontWeight = FontWeight.SemiBold,
                color = textColor,
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
}

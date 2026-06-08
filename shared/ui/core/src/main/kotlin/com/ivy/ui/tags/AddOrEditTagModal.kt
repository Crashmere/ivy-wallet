package com.ivy.ui.tags

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.ui.modal.IvyModal
import com.ivy.ui.modal.ModalPositiveButton
import com.ivy.ui.modal.ModalTitle
import com.ivy.ui.R
import com.ivy.ui.compose.GradientIconButton
import com.ivy.ui.platform.hideKeyboard
import com.ivy.ui.theme.colors.IvyFixedColors
import com.ivy.ui.theme.colors.IvyGradients
import java.util.UUID

@Suppress("DEPRECATION")
@SuppressLint("ComposeModifierMissing")
@ExperimentalFoundationApi
@Composable
internal fun BoxWithConstraintsScope.AddOrEditTagModal(
    id: UUID,
    visible: Boolean = false,
    initialTag: TagModalTag? = null,
    onTagAdd: (String) -> Unit = {},
    onTagEdit: (oldTag: TagModalTag, newName: String) -> Unit = { _, _ -> },
    onTagDelete: (TagModalTag) -> Unit = {},
    onDismiss: () -> Unit
) {
    val titleFocus = FocusRequester()

    var titleTextFieldValue by remember(id) {
        mutableStateOf(
            TextFieldValue(
                initialTag?.name ?: "",
                selection = TextRange(initialTag?.name?.length ?: 0)
            )
        )
    }

    var filename by remember(id) {
        mutableStateOf(initialTag?.name ?: "")
    }

    IvyModal(
        id = id,
        visible = visible,
        dismiss = onDismiss,
        PrimaryAction = {
            ModalPositiveButton(
                onClick = {
                    if (initialTag != null) {
                        onTagEdit(
                            initialTag,
                            filename
                        )
                    } else {
                        onTagAdd(filename)
                    }
                    onDismiss()
                },
                text = stringResource(R.string.done),
                iconStart = R.drawable.ic_custom_document_s,
                enabled = filename.isNotBlank()
            )
        }
    ) {
        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .padding(start = 0.dp, end = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModalTitle(text = if (initialTag == null) "Tag Name" else "Edit Tag Name")

            Spacer(modifier = Modifier.weight(1f))

            if (initialTag != null) {
                DeleteButton(
                    hasShadow = false,
                    onClick = {
                        onTagDelete(initialTag)
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        IvyTitleTextField(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .focusRequester(titleFocus),
            dividerModifier = Modifier
                .padding(horizontal = 24.dp),
            value = titleTextFieldValue,
            hint = "Enter TagName",
            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions(
                onNext = {}
            )
        ) {
            titleTextFieldValue = it
            filename = it.text
        }

        LaunchedEffect(titleFocus) {
            titleFocus.requestFocus()
        }
    }
}

@Composable
private fun DeleteButton(
    modifier: Modifier = Modifier,
    hasShadow: Boolean = true,
    onClick: () -> Unit,
) {
    GradientIconButton(
        modifier = modifier
            .size(48.dp)
            .testTag("delete_button"),
        backgroundPadding = 6.dp,
        icon = R.drawable.ic_delete,
        backgroundGradient = IvyGradients.Red,
        enabled = true,
        hasShadow = hasShadow,
        tint = IvyFixedColors.White,
        onClick = onClick
    )
}

@Composable
private fun IvyTitleTextField(
    modifier: Modifier = Modifier,
    dividerModifier: Modifier = Modifier,
    value: TextFieldValue,
    textColor: Color = TagModalTheme.colors.pureInverse,
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
                style = TagModalTheme.typo.h2.copy(
                    color = TagModalTheme.colors.gray,
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
            textStyle = TagModalTheme.typo.h2.copy(
                color = textColor,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Start
            ),
            singleLine = false,
            cursorBrush = SolidColor(TagModalTheme.colors.pureInverse),
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
            .background(TagModalTheme.colors.medium, TagModalTheme.shapes.rFull),
    )
}

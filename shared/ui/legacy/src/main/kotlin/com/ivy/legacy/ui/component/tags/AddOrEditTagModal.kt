package com.ivy.legacy.ui.component.tags

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ivy.data.model.Tag
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.model.TagId
import com.ivy.legacy.ui.component.DeleteButton
import com.ivy.legacy.ui.component.IvyTitleTextField
import com.ivy.legacy.ui.modal.IvyModal
import com.ivy.legacy.ui.modal.ModalPositiveButton
import com.ivy.legacy.ui.modal.ModalTitle
import com.ivy.ui.R

@Suppress("DEPRECATION")
@SuppressLint("ComposeModifierMissing")
@ExperimentalFoundationApi
@Composable
internal fun BoxWithConstraintsScope.AddOrEditTagModal(
    id: TagId,
    visible: Boolean = false,
    initialTag: Tag? = null,
    onTagAdd: (String) -> Unit = {},
    onTagEdit: (oldTag: Tag, newTag: Tag) -> Unit = { _, _ -> },
    onTagDelete: (Tag) -> Unit = {},
    onDismiss: () -> Unit
) {
    val titleFocus = FocusRequester()

    var titleTextFieldValue by remember(id) {
        mutableStateOf(
            TextFieldValue(
                initialTag?.name?.value ?: "",
                selection = TextRange(initialTag?.name?.value?.length ?: 0)
            )
        )
    }

    var filename by remember(id) {
        mutableStateOf(initialTag?.name?.value ?: "")
    }

    IvyModal(
        id = id.value,
        visible = visible,
        dismiss = onDismiss,
        PrimaryAction = {
            ModalPositiveButton(
                onClick = {
                    if (initialTag != null) {
                        onTagEdit(
                            initialTag,
                            initialTag.copy(
                                name = NotBlankTrimmedString.unsafe(filename)
                            )
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

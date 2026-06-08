package com.ivy.legacy.ui.modal.edit

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
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
import com.ivy.data.model.Category
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.ui.platform.hideKeyboard
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.R
import com.ivy.data.model.CreateCategoryData
import com.ivy.legacy.ui.theme.Ivy
import com.ivy.ui.icon.ItemIconMDefaultIcon
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.dynamicContrast
import com.ivy.legacy.ui.modal.ChooseIconModal
import com.ivy.legacy.ui.modal.IvyModal
import com.ivy.legacy.ui.modal.ModalAddSave
import com.ivy.legacy.ui.modal.ModalTitle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.CategoryModal(
    visible: Boolean,
    category: Category?,
    autoFocusKeyboard: Boolean = true,
    onCreateCategory: (CreateCategoryData) -> Unit,
    onEditCategory: (Category) -> Unit,
    dismiss: () -> Unit,
) {
    val initialCategory = category
    var nameTextFieldValue by remember(visible, initialCategory) {
        mutableStateOf(selectEndTextFieldValue(initialCategory?.name?.value))
    }
    var color by remember(visible, initialCategory) {
        mutableStateOf(initialCategory?.color?.let { Color(it.value) } ?: Ivy)
    }
    var icon by remember(visible, initialCategory) {
        mutableStateOf(initialCategory?.icon)
    }

    var chooseIconModalVisible by remember(visible, initialCategory) {
        mutableStateOf(false)
    }
    val modalId = remember(visible, initialCategory) {
        if (visible) UUID.randomUUID() else null
    }

    IvyModal(
        id = modalId,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ModalAddSave(
                item = category,
                enabled = nameTextFieldValue.text.isNullOrBlank().not()
            ) {
                if (initialCategory != null) {
                    onEditCategory(
                        initialCategory.copy(
                            name = NotBlankTrimmedString.unsafe(nameTextFieldValue.text.trim()),
                            color = ColorInt(color.toArgb()),
                            icon = icon
                        )
                    )
                } else {
                    onCreateCategory(
                        CreateCategoryData(
                            name = nameTextFieldValue.text.trim(),
                            color = color.toArgb(),
                            icon = icon?.id
                        )
                    )
                }

                dismiss()
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        ModalTitle(
            text = if (category != null) {
                stringResource(R.string.edit_category)
            } else {
                stringResource(
                    R.string.create_category
                )
            }
        )

        Spacer(Modifier.height(24.dp))

        IconNameRow(
            hint = stringResource(R.string.category_name),
            defaultIcon = R.drawable.ic_custom_category_m,
            color = color,
            icon = icon?.id,

            autoFocusKeyboard = autoFocusKeyboard,

            nameTextFieldValue = nameTextFieldValue,
            setNameTextFieldValue = { nameTextFieldValue = it },
            showChooseIconModal = {
                chooseIconModalVisible = true
            }
        )

        Spacer(Modifier.height(40.dp))

        IvyColorPicker(
            selectedColor = color,
            onColorSelected = { color = it }
        )

        Spacer(Modifier.height(48.dp))
    }

    ChooseIconModal(
        visible = chooseIconModalVisible,
        initialIcon = icon?.id ?: "category",
        color = color,
        dismiss = { chooseIconModalVisible = false }
    ) {
        icon = it?.let { iconId -> IconAsset.unsafe(iconId) }
    }
}

@Composable
private fun IvyNameTextField(
    modifier: Modifier = Modifier,
    underlineModifier: Modifier = Modifier,
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
    focusRequester: FocusRequester = remember { FocusRequester() },
    keyboardActions: KeyboardActions? = null,
    onValueChanged: (TextFieldValue) -> Unit
) {
    Column {
        val isEmpty = value.text.isBlank()

        Box(
            modifier = modifier,
            contentAlignment = Alignment.CenterStart
        ) {
            if (isEmpty && hint.isNullOrBlank().not()) {
                Text(
                    text = hint!!,
                    style = LegacyTheme.typo.b2.copy(
                        color = LegacyTheme.colors.gray,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start
                    ),
                )
            }

            val view = LocalView.current
            BasicTextField(
                modifier = Modifier
                    .testTag("base_input")
                    .focusRequester(focusRequester),
                value = value,
                onValueChange = onValueChanged,
                textStyle = LegacyTheme.typo.b1.copy(
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold,
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

        IvyDividerLineRounded(
            modifier = underlineModifier
        )
    }
}

@Composable
private fun IvyDividerLineRounded(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.rFull)
    )
}

@Composable
internal fun IconNameRow(
    hint: String,
    @DrawableRes defaultIcon: Int,
    color: Color,
    icon: String?,

    autoFocusKeyboard: Boolean,

    nameTextFieldValue: TextFieldValue,
    setNameTextFieldValue: (TextFieldValue) -> Unit,

    showChooseIconModal: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val nameFocus = FocusRequester()

        onCompositionStart {
            if (autoFocusKeyboard) {
                nameFocus.requestFocus()
            }
        }

        Spacer(Modifier.width(24.dp))

        ItemIconMDefaultIcon(
            modifier = Modifier
                .clip(CircleShape)
                .background(color, CircleShape)
                .clickable {
                    showChooseIconModal()
                }
                .testTag("modal_item_icon"),
            iconName = icon,
            tint = color.dynamicContrast(),
            defaultIcon = defaultIcon
        )

        val view = LocalView.current
        IvyNameTextField(
            modifier = Modifier
                .padding(start = 28.dp, end = 36.dp)
                .focusRequester(nameFocus),
            underlineModifier = Modifier.padding(start = 24.dp, end = 32.dp),
            value = nameTextFieldValue,
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
            setNameTextFieldValue(newValue)
        }
    }
}

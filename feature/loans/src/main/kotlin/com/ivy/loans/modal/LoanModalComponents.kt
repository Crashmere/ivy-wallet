package com.ivy.loans.modal

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.button.IvyCircleButton
import com.ivy.legacy.ui.button.IvyOutlinedButton
import com.ivy.legacy.ui.icon.ItemIconMDefaultIcon
import com.ivy.legacy.ui.modal.ModalAdd
import com.ivy.legacy.ui.modal.ModalSave
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.legacy.ui.theme.dynamicContrast
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.R
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.platform.hideKeyboard
import com.ivy.ui.time.LocalTimeFormatter
import com.ivy.ui.time.formatNicely
import java.time.LocalDateTime

@Composable
internal fun LoanModalAddSave(
    isEdit: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    if (isEdit) {
        ModalSave(
            enabled = enabled,
            onClick = onClick
        )
    } else {
        ModalAdd(
            enabled = enabled,
            onClick = onClick
        )
    }
}

@Composable
internal fun LoanDateTimeRow(
    dateTime: LocalDateTime,
    onEditDate: () -> Unit,
    onEditTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = LocalTimeFormatter.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        IvyOutlinedButton(
            text = dateTime.formatNicely(),
            iconStart = R.drawable.ic_date,
            onClick = onEditDate
        )

        Spacer(Modifier.weight(1f))

        IvyOutlinedButton(
            text = with(timeFormatter) {
                dateTime.toLocalTime().format()
            },
            iconStart = R.drawable.ic_date,
            onClick = onEditTime
        )

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
internal fun LoanIconNameRow(
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
        Column(
            modifier = Modifier
                .padding(start = 28.dp, end = 36.dp)
        ) {
            Box(
                contentAlignment = Alignment.CenterStart
            ) {
                if (nameTextFieldValue.text.isBlank()) {
                    Text(
                        text = hint,
                        style = LegacyTheme.typo.b2.style(
                            color = LegacyTheme.colors.gray,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Start
                        ),
                    )
                }

                BasicTextField(
                    modifier = Modifier
                        .testTag("base_input")
                        .focusRequester(nameFocus),
                    value = nameTextFieldValue,
                    onValueChange = setNameTextFieldValue,
                    textStyle = LegacyTheme.typo.b1.style(
                        color = LegacyTheme.colors.pureInverse,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start
                    ),
                    singleLine = false,
                    cursorBrush = SolidColor(LegacyTheme.colors.pureInverse),
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
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(LegacyTheme.colors.medium, LegacyTheme.shapes.rFull)
            )
        }
    }
}

@Composable
internal fun LoanModalNameInput(
    hint: String,
    autoFocusKeyboard: Boolean,
    textFieldValue: TextFieldValue,
    setTextFieldValue: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nameFocus = FocusRequester()

    onCompositionStart {
        if (autoFocusKeyboard) {
            nameFocus.requestFocus()
        }
    }

    val view = LocalView.current
    Box(
        modifier = modifier
            .padding(start = 32.dp, end = 36.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (textFieldValue.text.isBlank()) {
            Text(
                text = hint,
                style = LegacyTheme.typo.b2.style(
                    color = LegacyTheme.colors.gray,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                )
            )
        }

        BasicTextField(
            modifier = Modifier
                .testTag("base_input")
                .focusRequester(nameFocus),
            value = textFieldValue,
            onValueChange = setTextFieldValue,
            textStyle = LegacyTheme.typo.b1.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            ),
            singleLine = false,
            cursorBrush = SolidColor(LegacyTheme.colors.pureInverse),
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
            )
        )
    }

    Spacer(Modifier.height(8.dp))

    Spacer(
        modifier = Modifier
            .padding(start = 32.dp, end = 32.dp)
            .fillMaxWidth()
            .height(2.dp)
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.rFull)
    )
}

@Composable
internal fun LoanModalDelete(
    enabled: Boolean = true,
    testTag: String = "modal_delete",
    @DrawableRes icon: Int = R.drawable.ic_delete,
    onClick: () -> Unit
) {
    IvyCircleButton(
        modifier = Modifier
            .size(40.dp)
            .testTag(testTag),
        icon = icon,
        backgroundGradient = Gradient(LegacyTheme.colors.red, Color(0xFFFF99AB)),
        enabled = enabled,
        tint = White,
        onClick = onClick
    )
}

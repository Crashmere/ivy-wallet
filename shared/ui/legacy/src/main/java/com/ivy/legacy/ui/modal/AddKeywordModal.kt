package com.ivy.legacy.ui.modal

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.theme.system.style
import com.ivy.legacy.ui.onScreenStart
import com.ivy.legacy.ui.selectEndTextFieldValue
import com.ivy.ui.R
import com.ivy.legacy.ui.component.IvyTitleTextField
import java.util.UUID
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Suppress("ParameterNaming")
@Composable
fun BoxWithConstraintsScope.AddKeywordModal(
    id: UUID = UUID.randomUUID(),
    keyword: String,
    visible: Boolean,
    dismiss: () -> Unit,
    onKeywordChanged: (String) -> Unit
) {
    var modalKeyword by remember { mutableStateOf(selectEndTextFieldValue(keyword)) }

    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ModalAdd {
                onKeywordChanged(modalKeyword.text)
                dismiss()
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            modifier = Modifier.padding(start = 32.dp),
            text = stringResource(R.string.add_keyword),
            style = LegacyTheme.typo.b1.style(
                fontWeight = FontWeight.ExtraBold,
                color = LegacyTheme.colors.pureInverse
            )
        )

        Spacer(Modifier.height(32.dp))

        val inputFocus = FocusRequester()

        onScreenStart {
            inputFocus.requestFocus()
        }

        IvyTitleTextField(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .focusRequester(inputFocus),
            dividerModifier = Modifier.padding(horizontal = 24.dp),
            value = modalKeyword,
            hint = stringResource(R.string.keyword)
        ) {
            modalKeyword = it
        }

        Spacer(Modifier.height(48.dp))
    }
}
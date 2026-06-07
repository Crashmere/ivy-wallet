package com.ivy.wallet.ui.theme.modal

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
import com.ivy.design.l0_system.LegacyTheme
import com.ivy.design.l0_system.style
import com.ivy.ui.legacy.onScreenStart
import com.ivy.ui.legacy.selectEndTextFieldValue
import com.ivy.ui.R
import com.ivy.legacy.ui.component.IvyTitleTextField
import java.util.UUID
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Deprecated("Old design system. Use `:ivy-design` and Material3")
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
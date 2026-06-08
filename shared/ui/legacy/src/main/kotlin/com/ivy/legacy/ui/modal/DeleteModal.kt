package com.ivy.legacy.ui.modal

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Red
import java.util.UUID

@SuppressLint("ComposeModifierMissing")
@Composable
fun BoxWithConstraintsScope.DeleteModal(
    title: String,
    description: String,
    visible: Boolean,
    dismiss: () -> Unit,
    id: UUID = UUID.randomUUID(),
    buttonText: String = stringResource(R.string.delete),
    iconStart: Int = R.drawable.ic_delete,
    onDelete: () -> Unit,
) {
    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ModalNegativeButton(
                text = buttonText,
                iconStart = iconStart
            ) {
                onDelete()
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = title,
            style = LegacyTheme.typo.b1.copy(
                color = Red,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(24.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = description,
            style = LegacyTheme.typo.b2.copy(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(48.dp))
    }
}

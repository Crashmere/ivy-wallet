package com.ivy.legacy.ui.modal

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.Red
import com.ivy.ui.modal.IvyModal
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.ProgressModal(
    id: UUID = UUID.randomUUID(),
    title: String,
    description: String,
    visible: Boolean,
    color: Color = LegacyTheme.colors.orange,
    dismiss: () -> Unit = {},
) {
    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {}
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

        Spacer(Modifier.height(24.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(8.dp)
                .clip(
                    LegacyTheme.shapes.rFull
                ),
            color = color
        )

        Spacer(Modifier.height(48.dp))
    }
}

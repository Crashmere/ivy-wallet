package com.ivy.ui.modal

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ModalTitle(
    text: String
) {
    Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = text,
        style = ModalTitleTheme.typo.b1.copy(
            color = ModalTitleTheme.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start
        )
    )
}

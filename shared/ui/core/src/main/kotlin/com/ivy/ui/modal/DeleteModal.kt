package com.ivy.ui.modal

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.ui.R
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.theme.colors.Gradient
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
    @DrawableRes iconStart: Int = R.drawable.ic_delete,
    onDelete: () -> Unit,
) {
    val theme = ModalStatusTheme
    val colors = theme.colors
    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            DeleteModalButton(
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
            style = theme.typo.b1.copy(
                color = colors.red,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(24.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = description,
            style = theme.typo.b2.copy(
                color = colors.pureInverse,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun DeleteModalButton(
    text: String,
    @DrawableRes iconStart: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val theme = ModalStatusTheme
    val colors = theme.colors
    GradientButton(
        text = text,
        backgroundGradient = Gradient(colors.red, Color(0xFFFF99AB)),
        disabledBackgroundColor = colors.gray,
        shape = theme.shapes.rFull,
        textStyle = theme.typo.b2.copy(
            color = colors.white,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        iconStart = iconStart,
        iconTint = colors.white,
        onClick = onClick,
        enabled = enabled
    )
}

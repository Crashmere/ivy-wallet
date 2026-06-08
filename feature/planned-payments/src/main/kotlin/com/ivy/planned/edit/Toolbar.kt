package com.ivy.planned.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.CopyAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivy.data.model.TransactionType
import com.ivy.legacy.ui.button.IvyCircleButton
import com.ivy.legacy.ui.button.IvyOutlinedButton
import com.ivy.ui.theme.colors.Gradient
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.ui.R
import com.ivy.ui.compose.CloseIconButton
import java.util.UUID

@Composable
internal fun Toolbar(
    type: TransactionType,
    initialTransactionId: UUID?,
    onClose: () -> Unit,
    onDeleteTransactionModal: () -> Unit,
    onChangeTransactionTypeModal: () -> Unit,
    showDuplicateButton: Boolean,
    onDuplicate: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(24.dp))

        CloseButton(onClick = onClose)

        Spacer(Modifier.weight(1f))

        when (type) {
            TransactionType.INCOME -> {
                IvyOutlinedButton(
                    text = stringResource(R.string.income),
                    iconStart = R.drawable.ic_income,
                    onClick = onChangeTransactionTypeModal
                )

                Spacer(Modifier.width(12.dp))
            }

            TransactionType.EXPENSE -> {
                IvyOutlinedButton(
                    text = stringResource(R.string.expense),
                    iconStart = R.drawable.ic_expense,
                    onClick = onChangeTransactionTypeModal
                )

                Spacer(Modifier.width(12.dp))
            }

            TransactionType.TRANSFER -> Unit
        }

        if (initialTransactionId != null) {
            if (showDuplicateButton) {
                DuplicateButton(onClick = onDuplicate)
                Spacer(Modifier.width(12.dp))
            }

            DeleteToolbarButton(onClick = onDeleteTransactionModal)

            Spacer(Modifier.width(24.dp))
        }
    }
}

@Composable
private fun DuplicateButton(onClick: () -> Unit) {
    OutlinedIconButton(
        modifier = Modifier
            .size(48.dp)
            .background(Color.Transparent, CircleShape)
            .testTag("duplicate_button"),
        shape = CircleShape,
        colors = IconButtonDefaults.outlinedIconButtonColors()
            .copy(contentColor = LegacyTheme.colors.medium),
        border = BorderStroke(width = 2.dp, color = LegacyTheme.colors.medium),
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.padding(6.dp),
            imageVector = Icons.Sharp.CopyAll,
            contentDescription = "duplicate_button",
            tint = LegacyTheme.colors.pureInverse
        )
    }
}

@Composable
private fun DeleteToolbarButton(onClick: () -> Unit) {
    IvyCircleButton(
        modifier = Modifier
            .size(48.dp)
            .testTag("delete_button"),
        backgroundPadding = 6.dp,
        icon = R.drawable.ic_delete,
        backgroundGradient = Gradient(LegacyTheme.colors.red, Color(0xFFFF99AB)),
        enabled = true,
        hasShadow = false,
        tint = White,
        onClick = onClick
    )
}

@Composable
private fun CloseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CloseIconButton(
        modifier = modifier,
        backgroundColor = LegacyTheme.colors.pure,
        borderColor = LegacyTheme.colors.medium,
        tint = LegacyTheme.colors.pureInverse,
        onClick = onClick,
    )
}

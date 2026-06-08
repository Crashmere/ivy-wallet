package com.ivy.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.modal.IvyModal
import com.ivy.legacy.ui.modal.ModalTitle
import com.ivy.ui.theme.colors.IvyFixedColors.Ivy
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.R
import com.ivy.ui.compose.thenIf
import java.util.UUID

@Composable
internal fun BoxWithConstraintsScope.SettingsStartDateOfMonthModal(
    id: UUID = UUID.randomUUID(),
    visible: Boolean,
    selectedStartDateOfMonth: Int,
    dismiss: () -> Unit,
    onStartDateOfMonthSelected: (Int) -> Unit,
) {
    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = { }
    ) {
        Spacer(Modifier.height(32.dp))

        ModalTitle(text = stringResource(R.string.choose_start_date_of_month))

        Spacer(Modifier.height(32.dp))

        NumberRows(
            selectedNumber = selectedStartDateOfMonth,
            onClick = {
                onStartDateOfMonthSelected(it)
                dismiss()
            }
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ColumnScope.NumberRows(
    selectedNumber: Int,
    onClick: (Int) -> Unit,
) {
    val rowStarts = (1..31 step 5).toList()
    rowStarts.forEachIndexed { index, rowStart ->
        NumberRow(
            selectedNumber = selectedNumber,
            fromInclusive = rowStart,
            toInclusive = minOf(rowStart + 4, 31),
            onClick = onClick
        )

        if (index != rowStarts.lastIndex) {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ColumnScope.NumberRow(
    selectedNumber: Int,
    fromInclusive: Int,
    toInclusive: Int,
    onClick: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        for (number in fromInclusive..toInclusive) {
            NumberView(
                number = number,
                selected = number == selectedNumber,
                onClick = onClick
            )

            Spacer(Modifier.width(20.dp))
        }

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun NumberView(
    number: Int,
    selected: Boolean,
    onClick: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(2.dp, if (selected) Ivy else LegacyTheme.colors.medium, CircleShape)
            .thenIf(selected) {
                background(Ivy, CircleShape)
            }
            .clickable {
                onClick(number)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = LegacyTheme.typo.nB2.style(
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) White else LegacyTheme.colors.pureInverse,
                textAlign = TextAlign.Center
            )
        )
    }
}

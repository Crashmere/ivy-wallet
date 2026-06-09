package com.ivy.planned.edit

import com.ivy.planned.PlannedTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.IntervalType
import com.ivy.ui.time.forDisplay
import com.ivy.ui.time.formatDateOnly
import com.ivy.ui.R
import com.ivy.ui.compose.ResourceIcon
import java.time.LocalDateTime
import java.util.Locale

@Composable
internal fun RecurringRule(
    startDate: LocalDateTime?,
    intervalN: Int?,
    intervalType: IntervalType?,
    oneTime: Boolean,
    onShowRecurringRuleModal: () -> Unit,
) {
    if (
        hasRecurringRule(
            startDate = startDate,
            intervalN = intervalN,
            intervalType = intervalType,
            oneTime = oneTime
        )
    ) {
        RecurringRuleCard(
            startDate = startDate!!,
            intervalN = intervalN,
            intervalType = intervalType,
            oneTime = oneTime,
            onClick = {
                onShowRecurringRuleModal()
            }
        )
    } else {
        AddRecurringRuleButton(onClick = onShowRecurringRuleModal)
    }
}

internal fun hasRecurringRule(
    startDate: LocalDateTime?,
    intervalN: Int?,
    intervalType: IntervalType?,
    oneTime: Boolean,
): Boolean {
    return startDate != null &&
        ((intervalN != null && intervalType != null) || oneTime)
}

@Composable
private fun AddRecurringRuleButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(PlannedTheme.shapes.r4)
            .background(PlannedTheme.colors.medium, PlannedTheme.shapes.r4)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        ResourceIcon(
            icon = R.drawable.ic_planned_payments,
            tint = PlannedTheme.colors.pureInverse
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.add_planned_date_payment),
            style = PlannedTheme.typo.b2.copy(
                color = PlannedTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }
}

@Composable
private fun RecurringRuleCard(
    startDate: LocalDateTime,
    intervalN: Int?,
    intervalType: IntervalType?,
    oneTime: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(PlannedTheme.shapes.r4)
            .background(PlannedTheme.colors.medium, PlannedTheme.shapes.r4)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        ResourceIcon(
            icon = R.drawable.ic_planned_payments,
            tint = PlannedTheme.colors.pureInverse
        )

        Spacer(Modifier.width(8.dp))

        Column {
            Text(
                text = if (oneTime) stringResource(R.string.planned_for) else stringResource(R.string.planned_start_at),
                style = PlannedTheme.typo.b2.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PlannedTheme.colors.pureInverse,
                    textAlign = TextAlign.Start
                )
            )

            if (!oneTime && intervalType != null && intervalN != null) {
                Spacer(Modifier.height(4.dp))

                val intervalTypeLabel = intervalType.forDisplay(intervalN).uppercase(Locale.getDefault())
                Text(
                    text = stringResource(R.string.repeats_every, intervalN, intervalTypeLabel),
                    style = PlannedTheme.typo.c.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PlannedTheme.colors.orange,
                        textAlign = TextAlign.Start
                    )
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = startDate.toLocalDate().formatDateOnly(),
            style = PlannedTheme.typo.nB2.copy(
                color = PlannedTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.width(24.dp))
    }
}

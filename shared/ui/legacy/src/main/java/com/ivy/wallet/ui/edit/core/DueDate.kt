package com.ivy.wallet.ui.edit.core

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.design.api.LocalTimeFormatter
import com.ivy.design.l0_system.LegacyTheme
import com.ivy.design.l0_system.style
import com.ivy.ui.R
import com.ivy.ui.time.TimeFormatter
import com.ivy.wallet.ui.theme.components.IvyIcon
import java.time.Instant
import com.ivy.design.api.LocalTimeProvider
import java.util.concurrent.TimeUnit

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun DueDate(
    dueDate: Instant,
    onPickDueDate: () -> Unit,
) {
    DueDateCard(
        dueDate = dueDate,
        onClick = {
            onPickDueDate()
        }
    )
}

@Composable
private fun DueDateCard(
    dueDate: Instant,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(LegacyTheme.shapes.r4)
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.r4)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        IvyIcon(icon = R.drawable.ic_planned_payments)

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.planned_for),
            style = LegacyTheme.typo.b2.style(
                fontWeight = FontWeight.ExtraBold,
                color = LegacyTheme.colors.pureInverse
            )
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = with(LocalTimeFormatter.current) {
                dueDate.formatLocal(TimeFormatter.Style.DateOnly(includeWeekDay = false))
            },
            style = LegacyTheme.typo.nB2.style(
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.width(24.dp))
    }
}

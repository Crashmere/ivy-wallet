package com.ivy.legacy.ui.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.data.model.currency.format
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.R
import com.ivy.ui.time.formatLocal
import com.ivy.ui.time.LocalTimeProvider
import com.ivy.ui.theme.colors.IvyFixedColors.Gray
import com.ivy.ui.theme.colors.IvyFixedColors.Green
import java.time.LocalDate

@Composable
internal fun HistoryDateDivider(
    date: LocalDate,
    spacerTop: Dp,
    baseCurrency: String,
    income: Double,
    expenses: Double
) {
    Spacer(Modifier.height(spacerTop))

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        val today = LocalTimeProvider.current.localDateNow()

        Column {
            Text(
                text = date.formatLocal(
                    if (today.year == date.year) "MMMM dd." else "MMM dd. yyy"
                ),
                style = LegacyTheme.typo.b1.copy(
                    color = LegacyTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                )
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = when (date) {
                    today -> {
                        stringResource(R.string.today)
                    }
                    today.minusDays(1) -> {
                        stringResource(R.string.yesterday)
                    }
                    today.plusDays(1) -> {
                        stringResource(R.string.tomorrow)
                    }
                    else -> {
                        date.formatLocal("EEEE")
                    }
                },
                style = LegacyTheme.typo.c.copy(
                    color = LegacyTheme.colors.pureInverse,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
            )
        }

        Spacer(Modifier.weight(1f))

        val cashflow = income - expenses
        Text(
            text = "${cashflow.format(baseCurrency)} $baseCurrency",
            style = LegacyTheme.typo.nB2.copy(
                fontWeight = FontWeight.Bold,
                color = if (cashflow > 0) Green else Gray,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.width(32.dp))
    }

    Spacer(Modifier.height(4.dp))
}

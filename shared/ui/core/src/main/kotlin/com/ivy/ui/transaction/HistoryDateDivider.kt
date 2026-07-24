package com.ivy.ui.transaction

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.ui.R
import com.ivy.ui.money.formatAmount
import com.ivy.ui.time.formatLocal
import com.ivy.ui.time.LocalTimeProvider
import com.ivy.ui.theme.colors.IvyFixedColors.Gray
import com.ivy.ui.theme.colors.IvyFixedColors.Green
import java.time.LocalDate
import kotlin.math.absoluteValue

@Composable
internal fun HistoryDateDivider(
    date: LocalDate,
    spacerTop: Dp,
    baseCurrency: String,
    income: Double,
    expenses: Double
) {
    Spacer(Modifier.height(spacerTop))

    val today = LocalTimeProvider.current.localDateNow()

    val datePart = date.formatLocal(
        if (today.year == date.year) "M月d日" else "yyyy年M月d日"
    )
    val relativePart = when (date) {
        today -> stringResource(R.string.today)
        today.minusDays(1) -> stringResource(R.string.yesterday)
        today.plusDays(1) -> stringResource(R.string.tomorrow)
        else -> date.formatLocal("EEEE")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$datePart · $relativePart",
            style = TransactionListTheme.typo.nC.copy(
                color = TransactionListTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.weight(1f))

        val cashflow = income - expenses
        val sign = if (cashflow >= 0) "+" else "-"
        Text(
            text = "$sign${formatAmount(cashflow.absoluteValue, baseCurrency)}",
            style = TransactionListTheme.typo.nC.copy(
                fontWeight = FontWeight.Bold,
                color = when {
                    cashflow > 0 -> Green
                    cashflow < 0 -> TransactionListTheme.colors.pureInverse
                    else -> Gray
                },
                textAlign = TextAlign.End
            )
        )
    }

    Spacer(Modifier.height(10.dp))
}

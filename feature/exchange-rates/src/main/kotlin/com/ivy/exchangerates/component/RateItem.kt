package com.ivy.exchangerates.component

import com.ivy.exchangerates.ExchangeRatesTheme

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.currency.format
import com.ivy.exchangerates.model.RateUi

@Composable
internal fun RateItem(
    rate: RateUi,
    onDelete: (() -> Unit)?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(
                horizontal = 16.dp
            )
            .clickable(onClick = onClick)
            .border(2.dp, ExchangeRatesTheme.colors.medium, ExchangeRatesTheme.shapes.r4)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            val currencyValue: Double = 1.0
            RateColumn(
                label = "Sell",
                rate = rate.from,
                value = currencyValue.format(currencyCode = rate.from)
            )

            Spacer(Modifier.width(16.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "arrow to next"
            )
            Spacer(Modifier.width(16.dp))
            RateColumn(
                label = "Buy",
                rate = rate.to,
                value = rate.rate.format(currencyCode = rate.to)
            )

            if (onDelete != null) {
                Spacer(Modifier.weight(1f))
                ExchangeRateDeleteButton(onClick = onDelete)
            }
        }
    }
}

@Composable
private fun RateColumn(label: String, rate: String, value: String) {
    Column {
        Text(
            text = label,
            style = ExchangeRatesTheme.typo.c.copy(
                color = ExchangeRatesTheme.colors.pureInverse,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Normal
            )
        )
        Text(
            text = rate,
            style = ExchangeRatesTheme.typo.nB1.copy(
                color = ExchangeRatesTheme.colors.pureInverse,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Text(
            text = value,
            style = ExchangeRatesTheme.typo.nB2.copy(
                color = ExchangeRatesTheme.colors.pureInverse,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Normal
            )
        )
    }
}

package com.ivy.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.data.model.currency.format
import com.ivy.ui.R
import com.ivy.ui.compose.ResourceIcon

@Composable
internal fun CustomExchangeRateCard(
    fromCurrencyCode: String,
    toCurrencyCode: String,
    exchangeRate: Double,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.exchange_rate),
    onRefresh: () -> Unit = {},
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(EditTransactionTheme.shapes.r4)
            .background(EditTransactionTheme.colors.medium, EditTransactionTheme.shapes.r4)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.width(16.dp))

        ResourceIcon(
            icon = R.drawable.ic_currency,
            tint = EditTransactionTheme.colors.pureInverse
        )

        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = title,
                style = EditTransactionTheme.typo.b2.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = EditTransactionTheme.colors.pureInverse,
                    textAlign = TextAlign.Start
                )
            )

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fromCurrencyCode,
                    style = EditTransactionTheme.typo.b2.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = EditTransactionTheme.colors.orange,
                        textAlign = TextAlign.Start
                    )
                )
                ResourceIcon(
                    icon = R.drawable.ic_arrow_right,
                    tint = EditTransactionTheme.colors.orange
                )
                Text(
                    text = toCurrencyCode,
                    style = EditTransactionTheme.typo.nB2.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = EditTransactionTheme.colors.orange,
                        textAlign = TextAlign.Start
                    )
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1",
                    style = EditTransactionTheme.typo.nB2.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = EditTransactionTheme.colors.orange,
                        textAlign = TextAlign.Start
                    )
                )
                ResourceIcon(
                    icon = R.drawable.ic_arrow_right,
                    tint = EditTransactionTheme.colors.orange
                )
                Text(
                    text = exchangeRate.format(IvyCurrency.getDecimalPlaces(toCurrencyCode)),
                    style = EditTransactionTheme.typo.nB2.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = EditTransactionTheme.colors.orange,
                        textAlign = TextAlign.Start
                    )
                )
            }
        }
        ResourceIcon(
            icon = R.drawable.ic_refresh,
            tint = EditTransactionTheme.colors.pureInverse,
            modifier = Modifier
                .padding(end = 16.dp)
                .clickable {
                    onRefresh()
                }
        )
    }
}

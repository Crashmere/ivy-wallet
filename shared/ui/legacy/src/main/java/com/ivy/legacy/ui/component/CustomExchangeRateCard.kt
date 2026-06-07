package com.ivy.legacy.ui.component

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
import androidx.compose.ui.unit.dp
import com.ivy.legacy.design.l0_system.LegacyTheme
import com.ivy.legacy.design.l0_system.style
import com.ivy.data.model.currency.format
import com.ivy.ui.R
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.legacy.ui.theme.Orange

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun CustomExchangeRateCard(
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
            .clip(LegacyTheme.shapes.r4)
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.r4)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.width(16.dp))

        IvyIcon(icon = R.drawable.ic_currency)

        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = title,
                style = LegacyTheme.typo.b2.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = LegacyTheme.colors.pureInverse
                )
            )

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fromCurrencyCode,
                    style = LegacyTheme.typo.b2.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = Orange
                    )
                )
                IvyIcon(icon = R.drawable.ic_arrow_right, tint = Orange)
                Text(
                    text = toCurrencyCode,
                    style = LegacyTheme.typo.nB2.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = Orange
                    )
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1",
                    style = LegacyTheme.typo.nB2.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = Orange
                    )
                )
                IvyIcon(icon = R.drawable.ic_arrow_right, tint = Orange)
                Text(
                    text = exchangeRate.format(IvyCurrency.getDecimalPlaces(toCurrencyCode)),
                    style = LegacyTheme.typo.nB2.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = Orange
                    )
                )
            }
        }
        IvyIcon(
            icon = R.drawable.ic_refresh,
            modifier = Modifier
                .padding(end = 16.dp)
                .clickable {
                    onRefresh()
                }
        )
    }
}
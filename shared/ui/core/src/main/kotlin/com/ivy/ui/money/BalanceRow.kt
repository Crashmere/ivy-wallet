package com.ivy.ui.money

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.ivy.data.model.currency.format
import com.ivy.data.model.currency.shortenAmount
import com.ivy.data.model.currency.shouldShortAmount

/**
 * Currency-only build: the currency code (e.g. CNY) is intentionally never shown.
 * The unused currency-related params are kept so existing call sites don't need to change.
 */
@Composable
fun BalanceRow(
    currency: String,
    balance: Double,
    modifier: Modifier = Modifier,
    textColor: Color = MoneyDisplayTheme.colors.pureInverse,
    hiddenMode: Boolean = false,
    @Suppress("UNUSED_PARAMETER") spacerCurrency: Dp = 12.dp,
    @Suppress("UNUSED_PARAMETER") currencyFontSize: TextUnit? = null,
    balanceFontSize: TextUnit? = null,
    @Suppress("UNUSED_PARAMETER") currencyUpfront: Boolean = true,
    balanceAmountPrefix: String? = null,
    shortenBigNumbers: Boolean = false,
    @Suppress("UNUSED_PARAMETER") doubleRowDisplay: Boolean = false,
) {
    val shortAmount = shortenBigNumbers && shouldShortAmount(balance)
    val integerPartFormatted = if (shortAmount) {
        shortenAmount(balance)
    } else {
        balance.format(currency)
    }

    val balanceText = when {
        hiddenMode -> "****"
        balanceAmountPrefix != null -> "$balanceAmountPrefix$integerPartFormatted"
        else -> integerPartFormatted
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = balanceText,
            style = MoneyDisplayTheme.typo.nH1.copy(
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                textAlign = TextAlign.Start,
                fontSize = balanceFontSize ?: MoneyDisplayTheme.typo.nH1.fontSize,
            ),
        )
    }
}

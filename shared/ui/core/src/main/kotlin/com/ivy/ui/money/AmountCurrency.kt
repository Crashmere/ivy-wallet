package com.ivy.ui.money

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.currency.format
import com.ivy.data.model.currency.shortenAmount
import com.ivy.data.model.currency.shouldShortAmount

@SuppressLint(
    "ComposeContentEmitterReturningValues",
    "ComposeMultipleContentEmitters",
    "ComposeModifierMissing",
)
@Composable
fun AmountCurrencyB1(
    amount: Double,
    currency: String,
    amountFontWeight: FontWeight = FontWeight.Bold,
    textColor: Color = MoneyDisplayTheme.colors.pureInverse,
    shortenBigNumbers: Boolean = false,
) {
    val shortAmount = shortenBigNumbers && shouldShortAmount(amount)
    val text = if (shortAmount) shortenAmount(amount) else amount.format(currency)
    Text(
        modifier = Modifier.testTag("amount_currency_b1"),
        text = text,
        style = MoneyDisplayTheme.typo.nB1.copy(
            fontWeight = amountFontWeight,
            color = textColor,
            textAlign = TextAlign.Start,
        ),
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
        text = currency,
        style = MoneyDisplayTheme.typo.nB1.copy(
            fontWeight = FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Start,
        ),
    )
}

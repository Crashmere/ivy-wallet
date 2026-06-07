package com.ivy.legacy.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.theme.system.style
import com.ivy.data.model.currency.format
import com.ivy.data.model.currency.shortenAmount
import com.ivy.data.model.currency.shouldShortAmount

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun BalanceRowMedium(
    currency: String,
    balance: Double,
    modifier: Modifier = Modifier,
    textColor: Color = LegacyTheme.colors.pureInverse,
    balanceAmountPrefix: String? = null,
    currencyUpfront: Boolean = true,
    shortenBigNumbers: Boolean = false,
    hiddenMode: Boolean = false,
) {
    BalanceRow(
        modifier = modifier,

        textColor = textColor,
        currency = currency,
        balance = balance,
        hiddenMode = hiddenMode,
        spacerCurrency = 12.dp,
        currencyFontSize = 24.sp,
        balanceFontSize = 26.sp,

        balanceAmountPrefix = balanceAmountPrefix,
        currencyUpfront = currencyUpfront,
        shortenBigNumbers = shortenBigNumbers
    )
}

@Composable
fun BalanceRowMini(
    currency: String,
    balance: Double,
    modifier: Modifier = Modifier,
    textColor: Color = LegacyTheme.colors.pureInverse,
    balanceAmountPrefix: String? = null,
    currencyUpfront: Boolean = true,
    shortenBigNumbers: Boolean = false,
    hiddenMode: Boolean = false,
    doubleRowDisplay: Boolean = false,
) {
    BalanceRow(
        modifier = modifier,

        textColor = textColor,
        currency = currency,
        balance = balance,
        hiddenMode = hiddenMode,
        spacerCurrency = 8.dp,
        currencyFontSize = 20.sp,
        balanceFontSize = 22.sp,

        balanceAmountPrefix = balanceAmountPrefix,
        currencyUpfront = currencyUpfront,
        shortenBigNumbers = shortenBigNumbers,
        doubleRowDisplay = doubleRowDisplay
    )
}

@Composable
fun BalanceRow(
    currency: String,
    balance: Double,
    modifier: Modifier = Modifier,
    textColor: Color = LegacyTheme.colors.pureInverse,
    hiddenMode: Boolean = false,
    spacerCurrency: Dp = 12.dp,
    currencyFontSize: TextUnit? = null,
    balanceFontSize: TextUnit? = null,
    currencyUpfront: Boolean = true,
    balanceAmountPrefix: String? = null,
    shortenBigNumbers: Boolean = false,
    doubleRowDisplay: Boolean = false,
) {
    val shortAmount = shortenBigNumbers && shouldShortAmount(balance)
    val integerPartFormatted = if (shortAmount) {
        shortenAmount(balance)
    } else {
        balance.format(currency)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (doubleRowDisplay) {
                Currency(
                    currency = currency,
                    textColor = textColor,
                    currencyFontSize = currencyFontSize
                )
                Spacer(Modifier.width(spacerCurrency))
            }

            if (!doubleRowDisplay) {
                val balanceText = when {
                    hiddenMode -> "****"
                    balanceAmountPrefix != null -> "$balanceAmountPrefix$integerPartFormatted"
                    else -> integerPartFormatted
                }

                val balanceCurrencyText = if (currencyUpfront) {
                    "$currency $balanceText"
                } else {
                    "$balanceText $currency"
                }

                Text(
                    text = balanceCurrencyText,
                    style = if (balanceFontSize == null) {
                        LegacyTheme.typo.nH1.style(
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                    } else {
                        LegacyTheme.typo.nH1.style(
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        ).copy(fontSize = balanceFontSize)
                    }
                )
            }
        }

        if (doubleRowDisplay) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when {
                        hiddenMode -> "****"
                        balanceAmountPrefix != null -> "$balanceAmountPrefix$integerPartFormatted"
                        else -> integerPartFormatted
                    },
                    style = if (balanceFontSize == null) {
                        LegacyTheme.typo.nH1.style(
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                    } else {
                        LegacyTheme.typo.nH1.style(
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        ).copy(fontSize = balanceFontSize)
                    }
                )
            }
        }
    }
}

@Composable
private fun Currency(
    currency: String,
    currencyFontSize: TextUnit?,
    textColor: Color,
) {
    Text(
        text = currency,
        style = if (currencyFontSize == null) {
            LegacyTheme.typo.h1.style(
                fontWeight = FontWeight.Light,
                color = textColor
            )
        } else {
            LegacyTheme.typo.h1.style(
                fontWeight = FontWeight.Light,
                color = textColor
            ).copy(fontSize = currencyFontSize)
        }
    )
}
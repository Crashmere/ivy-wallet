package com.ivy.ui.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.money.BalanceRow

@Composable
fun ModalAmountSection(
    label: String,
    currency: String,
    amount: Double,
    Header: (@Composable () -> Unit)? = null,
    amountPaddingTop: Dp = 48.dp,
    amountPaddingBottom: Dp = 48.dp,
    showAmountModal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ModalDividerLine()

        Header?.invoke()

        Spacer(Modifier.height(amountPaddingTop))

        Text(
            text = label,
            style = ModalAmountSectionTheme.typo.c.copy(
                color = ModalAmountSectionTheme.colors.gray,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(4.dp))

        BalanceRow(
            modifier = Modifier
                .clickableNoIndication(rememberInteractionSource()) {
                    showAmountModal()
                }
                .testTag("amount_balance"),
            currency = currency,
            balance = amount,

            spacerCurrency = 8.dp,

            balanceFontSize = 40.sp,
            currencyFontSize = 30.sp,

            currencyUpfront = false
        )

        Spacer(Modifier.height(amountPaddingBottom))
    }
}

@Composable
private fun ModalDividerLine(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(ModalAmountSectionTheme.colors.medium)
    )
}

package com.ivy.exchangerates.modal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.exchangerates.model.RateUi
import com.ivy.ui.modal.IvyModal
import com.ivy.ui.modal.ModalAdd
import com.ivy.ui.modal.ModalTitle
import com.ivy.ui.modal.AmountModal
import com.ivy.ui.compose.selectEndTextFieldValue
import java.util.UUID

@Composable
internal fun BoxWithConstraintsScope.AddRateModal(
    visible: Boolean,
    baseCurrency: String,
    dismiss: () -> Unit,
    onAdd: (RateUi) -> Unit,
) {
    var toCurrency by remember { mutableStateOf(selectEndTextFieldValue("")) }
    var amountModalVisible by remember { mutableStateOf(false) }
    var rate by remember { mutableStateOf<Double?>(null) }

    IvyModal(
        id = null,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ModalAdd {
                val to = toCurrency
                val finalRate = rate
                onAdd(
                    RateUi(
                        from = baseCurrency,
                        to = to.text,
                        rate = finalRate ?: 0.0,
                    )
                )
                dismiss()
            }
        }
    ) {
        Spacer(Modifier.height(16.dp))
        ModalTitle(text = "Add rate")
        Spacer(Modifier.height(24.dp))

        ExchangeRateNameTextField(
            modifier = Modifier.padding(horizontal = 32.dp),
            underlineModifier = Modifier.padding(horizontal = 24.dp),
            value = toCurrency,
            hint = "Currency"
        ) {
            toCurrency = it
        }

        Spacer(Modifier.height(12.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    amountModalVisible = true
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            text = "$baseCurrency-${toCurrency.text} = ${rate ?: "???"}",
            style = LegacyTheme.typo.nH2.copy(
                color = LegacyTheme.colors.orange,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        )
        Spacer(Modifier.height(24.dp))
    }

    AmountModal(
        id = remember { UUID.randomUUID() },
        visible = amountModalVisible,
        currency = "",
        initialAmount = rate,
        decimalCountMax = 12,
        dismiss = { amountModalVisible = false },
        onAmountChanged = {
            rate = it
        }
    )
}

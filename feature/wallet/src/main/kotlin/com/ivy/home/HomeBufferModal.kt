package com.ivy.home

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivy.ui.modal.IvyModal
import com.ivy.ui.modal.ModalAmountSection
import com.ivy.ui.modal.ModalSave
import com.ivy.ui.modal.AmountModal
import com.ivy.ui.R
import java.util.UUID

@Composable
internal fun BoxWithConstraintsScope.HomeBufferModal(
    visible: Boolean,
    balance: Double,
    buffer: Double,
    currency: String,
    dismiss: () -> Unit,
    onBufferChanged: (Double) -> Unit,
) {
    val modalId = remember(visible) {
        UUID.randomUUID()
    }
    var newBufferAmount by remember(visible, buffer) {
        mutableStateOf(buffer)
    }

    var amountModalVisible by remember { mutableStateOf(false) }

    IvyModal(
        id = modalId,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ModalSave {
                onBufferChanged(newBufferAmount)
                dismiss()
            }
        }
    ) {
        Spacer(Modifier.height(16.dp))

        HomeBufferBattery(
            modifier = Modifier.padding(horizontal = 16.dp),
            buffer = newBufferAmount,
            balance = balance,
            currency = currency,
            backgroundNotFilled = HomeTheme.colors.medium,
        )

        Spacer(Modifier.height(24.dp))

        ModalAmountSection(
            label = stringResource(R.string.edit_savings_goal),
            currency = currency,
            amount = newBufferAmount
        ) {
            amountModalVisible = true
        }
    }

    val amountModalId = remember(visible, newBufferAmount) {
        UUID.randomUUID()
    }
    AmountModal(
        id = amountModalId,
        visible = amountModalVisible,
        currency = currency,
        initialAmount = newBufferAmount,
        dismiss = { amountModalVisible = false }
    ) {
        newBufferAmount = it
    }
}

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
import com.ivy.legacy.ui.modal.IvyModal
import com.ivy.legacy.ui.modal.ModalAmountSection
import com.ivy.legacy.ui.modal.ModalSave
import com.ivy.legacy.ui.modal.edit.AmountModal
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.R
import java.util.UUID

internal data class HomeBufferModalData(
    val balance: Double,
    val buffer: Double,
    val currency: String,
    val id: UUID = UUID.randomUUID(),
)

@Composable
internal fun BoxWithConstraintsScope.HomeBufferModal(
    modal: HomeBufferModalData?,
    dismiss: () -> Unit,
    onBufferChanged: (Double) -> Unit,
) {
    var newBufferAmount by remember(modal) {
        mutableStateOf(modal?.buffer ?: 0.0)
    }

    var amountModalVisible by remember { mutableStateOf(false) }

    IvyModal(
        id = modal?.id,
        visible = modal != null,
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
            balance = modal?.balance ?: 0.0,
            currency = modal?.currency ?: "",
            backgroundNotFilled = LegacyTheme.colors.medium,
        )

        Spacer(Modifier.height(24.dp))

        ModalAmountSection(
            label = stringResource(R.string.edit_savings_goal),
            currency = modal?.currency ?: "",
            amount = newBufferAmount
        ) {
            amountModalVisible = true
        }
    }

    val amountModalId = remember(modal, newBufferAmount) {
        UUID.randomUUID()
    }
    AmountModal(
        id = amountModalId,
        visible = amountModalVisible,
        currency = modal?.currency ?: "",
        initialAmount = newBufferAmount,
        dismiss = { amountModalVisible = false }
    ) {
        newBufferAmount = it
    }
}

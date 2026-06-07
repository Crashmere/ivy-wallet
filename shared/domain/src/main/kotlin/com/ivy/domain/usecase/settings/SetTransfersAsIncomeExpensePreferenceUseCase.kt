package com.ivy.domain.usecase.settings

import com.ivy.data.api.TransferBehaviorPreferenceStore
import javax.inject.Inject

class SetTransfersAsIncomeExpensePreferenceUseCase @Inject constructor(
    private val transferBehaviorPreferenceStore: TransferBehaviorPreferenceStore,
) {
    operator fun invoke(enabled: Boolean) {
        transferBehaviorPreferenceStore.transfersAsIncomeExpense = enabled
    }
}

package com.ivy.domain.usecase.settings

import com.ivy.data.api.TransferBehaviorPreferenceStore
import javax.inject.Inject

class GetTransfersAsIncomeExpensePreferenceUseCase @Inject constructor(
    private val transferBehaviorPreferenceStore: TransferBehaviorPreferenceStore,
) {
    operator fun invoke(): Boolean {
        return transferBehaviorPreferenceStore.transfersAsIncomeExpense
    }
}

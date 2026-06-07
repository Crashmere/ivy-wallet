package com.ivy.domain.usecase.settings

import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class SetTransfersAsIncomeExpensePreferenceUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore,
) {
    operator fun invoke(enabled: Boolean) {
        appPreferences.transfersAsIncomeExpense = enabled
    }
}

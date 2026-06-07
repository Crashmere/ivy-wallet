package com.ivy.domain.usecase.settings

import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class GetTransfersAsIncomeExpensePreferenceUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore,
) {
    operator fun invoke(): Boolean {
        return appPreferences.transfersAsIncomeExpense
    }
}

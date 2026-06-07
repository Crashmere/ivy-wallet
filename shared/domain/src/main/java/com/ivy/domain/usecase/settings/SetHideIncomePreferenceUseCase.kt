package com.ivy.domain.usecase.settings

import com.ivy.domain.preferences.AppPreferences
import javax.inject.Inject

class SetHideIncomePreferenceUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(enabled: Boolean) {
        appPreferences.hideIncome = enabled
    }
}

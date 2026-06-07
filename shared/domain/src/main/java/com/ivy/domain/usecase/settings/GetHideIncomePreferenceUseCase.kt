package com.ivy.domain.usecase.settings

import com.ivy.domain.preferences.AppPreferences
import javax.inject.Inject

class GetHideIncomePreferenceUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Boolean {
        return appPreferences.hideIncome
    }
}

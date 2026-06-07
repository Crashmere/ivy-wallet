package com.ivy.domain.usecase.settings

import com.ivy.data.api.SettingsPreferenceStore
import javax.inject.Inject

class GetHideIncomePreferenceUseCase @Inject constructor(
    private val appPreferences: SettingsPreferenceStore,
) {
    operator fun invoke(): Boolean {
        return appPreferences.hideIncome
    }
}

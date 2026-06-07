package com.ivy.domain.usecase.settings

import com.ivy.data.api.SettingsPreferenceStore
import javax.inject.Inject

class SetHideCurrentBalancePreferenceUseCase @Inject constructor(
    private val appPreferences: SettingsPreferenceStore,
) {
    operator fun invoke(enabled: Boolean) {
        appPreferences.hideCurrentBalance = enabled
    }
}

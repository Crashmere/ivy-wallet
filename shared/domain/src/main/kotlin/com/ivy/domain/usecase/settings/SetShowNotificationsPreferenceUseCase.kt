package com.ivy.domain.usecase.settings

import com.ivy.data.api.SettingsPreferenceStore
import javax.inject.Inject

class SetShowNotificationsPreferenceUseCase @Inject constructor(
    private val appPreferences: SettingsPreferenceStore,
) {
    operator fun invoke(enabled: Boolean) {
        appPreferences.showNotifications = enabled
    }
}

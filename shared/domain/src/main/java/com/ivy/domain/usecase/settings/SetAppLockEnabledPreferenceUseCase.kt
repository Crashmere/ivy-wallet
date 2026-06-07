package com.ivy.domain.usecase.settings

import com.ivy.domain.preferences.AppPreferences
import javax.inject.Inject

class SetAppLockEnabledPreferenceUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(enabled: Boolean) {
        appPreferences.appLockEnabled = enabled
    }
}

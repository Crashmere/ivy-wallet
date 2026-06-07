package com.ivy.domain.usecase.settings

import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class SetAppLockEnabledPreferenceUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore,
) {
    operator fun invoke(enabled: Boolean) {
        appPreferences.appLockEnabled = enabled
    }
}

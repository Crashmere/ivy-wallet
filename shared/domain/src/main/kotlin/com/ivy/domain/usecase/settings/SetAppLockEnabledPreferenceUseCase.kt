package com.ivy.domain.usecase.settings

import com.ivy.data.api.AppLockPreferenceStore
import javax.inject.Inject

class SetAppLockEnabledPreferenceUseCase @Inject internal constructor(
    private val appLockPreferenceStore: AppLockPreferenceStore,
) {
    operator fun invoke(enabled: Boolean) {
        appLockPreferenceStore.appLockEnabled = enabled
    }
}

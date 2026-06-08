package com.ivy.domain.usecase.settings

import com.ivy.data.api.AppLockPreferenceStore
import javax.inject.Inject

class GetAppLockEnabledPreferenceUseCase @Inject internal constructor(
    private val appLockPreferenceStore: AppLockPreferenceStore,
) {
    operator fun invoke(): Boolean {
        return appLockPreferenceStore.appLockEnabled
    }
}

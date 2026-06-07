package com.ivy.domain.usecase.settings

import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class IsInitialSetupCompletedUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore,
) {
    operator fun invoke(): Boolean {
        return appPreferences.initialSetupCompleted
    }
}

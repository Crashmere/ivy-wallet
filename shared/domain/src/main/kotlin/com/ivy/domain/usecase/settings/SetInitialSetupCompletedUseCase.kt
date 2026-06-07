package com.ivy.domain.usecase.settings

import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class SetInitialSetupCompletedUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore,
) {
    operator fun invoke(completed: Boolean) {
        appPreferences.initialSetupCompleted = completed
    }
}

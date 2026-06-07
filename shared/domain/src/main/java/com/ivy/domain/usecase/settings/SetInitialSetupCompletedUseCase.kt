package com.ivy.domain.usecase.settings

import com.ivy.domain.preferences.AppPreferences
import javax.inject.Inject

class SetInitialSetupCompletedUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(completed: Boolean) {
        appPreferences.initialSetupCompleted = completed
    }
}

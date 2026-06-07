package com.ivy.domain.usecase.reset

import com.ivy.domain.preferences.AppPreferences
import javax.inject.Inject

class ClearAppPreferencesUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke() {
        appPreferences.clearAll()
    }
}

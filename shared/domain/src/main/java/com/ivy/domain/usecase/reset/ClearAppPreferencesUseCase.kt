package com.ivy.domain.usecase.reset

import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class ClearAppPreferencesUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore,
) {
    operator fun invoke() {
        appPreferences.clearAll()
    }
}

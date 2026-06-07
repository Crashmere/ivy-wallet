package com.ivy.domain.usecase.reset

import com.ivy.data.api.AppPreferenceResetStore
import javax.inject.Inject

class ClearAppPreferencesUseCase @Inject constructor(
    private val appPreferenceResetStore: AppPreferenceResetStore,
) {
    operator fun invoke() {
        appPreferenceResetStore.clearAll()
    }
}

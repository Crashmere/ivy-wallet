package com.ivy.domain.usecase.reset

import com.ivy.data.api.LocalPreferenceResetStore
import javax.inject.Inject

class ClearLocalPreferencesUseCase @Inject internal constructor(
    private val localPreferenceResetStore: LocalPreferenceResetStore,
) {
    operator fun invoke() {
        localPreferenceResetStore.clearAll()
    }
}

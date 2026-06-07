package com.ivy.domain.usecase.settings

import com.ivy.data.api.SettingsStore
import javax.inject.Inject

class EnsureSettingsInitializedUseCase @Inject constructor(
    private val settingsStore: SettingsStore
) {
    suspend operator fun invoke(
        systemDarkMode: Boolean,
        currencyCode: String,
        bufferAmount: Double,
    ) {
        settingsStore.ensureInitialized(
            systemDarkMode = systemDarkMode,
            currencyCode = currencyCode,
            bufferAmount = bufferAmount,
        )
    }
}

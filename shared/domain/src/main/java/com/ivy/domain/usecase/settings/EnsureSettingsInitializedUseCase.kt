package com.ivy.domain.usecase.settings

import com.ivy.data.repository.SettingsRepository
import javax.inject.Inject

class EnsureSettingsInitializedUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        systemDarkMode: Boolean,
        currencyCode: String,
        bufferAmount: Double,
    ) {
        settingsRepository.ensureInitialized(
            systemDarkMode = systemDarkMode,
            currencyCode = currencyCode,
            bufferAmount = bufferAmount,
        )
    }
}

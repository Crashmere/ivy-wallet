package com.ivy.domain.usecase.settings

import com.ivy.data.repository.LegacySettingsRepository
import javax.inject.Inject

class EnsureSettingsInitializedUseCase @Inject constructor(
    private val legacySettingsRepository: LegacySettingsRepository
) {
    suspend operator fun invoke(
        systemDarkMode: Boolean,
        currencyCode: String,
        bufferAmount: Double,
    ) {
        legacySettingsRepository.ensureInitialized(
            systemDarkMode = systemDarkMode,
            currencyCode = currencyCode,
            bufferAmount = bufferAmount,
        )
    }
}

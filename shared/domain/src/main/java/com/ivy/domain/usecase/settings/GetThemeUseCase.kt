package com.ivy.domain.usecase.settings

import com.ivy.base.theme.Theme
import com.ivy.data.repository.LegacySettingsRepository
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val legacySettingsRepository: LegacySettingsRepository
) {
    suspend operator fun invoke(fallback: Theme = Theme.AUTO): Theme {
        return legacySettingsRepository.getTheme(fallback)
    }

    suspend fun withSystemFallback(systemDarkMode: Boolean): Theme {
        return legacySettingsRepository.getTheme(systemDarkMode)
    }
}

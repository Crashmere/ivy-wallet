package com.ivy.domain.usecase.settings

import com.ivy.base.theme.Theme
import com.ivy.data.repository.SettingsRepository
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(fallback: Theme = Theme.AUTO): Theme {
        return settingsRepository.getTheme(fallback)
    }

    suspend fun withSystemFallback(systemDarkMode: Boolean): Theme {
        return settingsRepository.getTheme(systemDarkMode)
    }
}

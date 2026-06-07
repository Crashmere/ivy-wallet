package com.ivy.domain.usecase.settings

import com.ivy.data.model.Theme
import com.ivy.data.api.SettingsStore
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val settingsStore: SettingsStore
) {
    suspend operator fun invoke(fallback: Theme = Theme.AUTO): Theme {
        return settingsStore.getTheme(fallback)
    }

    suspend fun withSystemFallback(systemDarkMode: Boolean): Theme {
        return settingsStore.getTheme(systemFallback(systemDarkMode))
    }

    private fun systemFallback(systemDarkMode: Boolean): Theme {
        return if (systemDarkMode) Theme.DARK else Theme.LIGHT
    }
}

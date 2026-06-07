package com.ivy.domain.usecase.settings

import com.ivy.data.api.ThemeStore
import com.ivy.data.model.Theme
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val themeStore: ThemeStore
) {
    suspend operator fun invoke(fallback: Theme = Theme.AUTO): Theme {
        return themeStore.getTheme(fallback)
    }

    suspend fun withSystemFallback(systemDarkMode: Boolean): Theme {
        return themeStore.getTheme(systemFallback(systemDarkMode))
    }

    private fun systemFallback(systemDarkMode: Boolean): Theme {
        return if (systemDarkMode) Theme.DARK else Theme.LIGHT
    }
}

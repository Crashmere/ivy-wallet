package com.ivy.domain.usecase.settings

import com.ivy.data.model.Theme
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
            defaultTheme = systemDefaultTheme(systemDarkMode),
            currencyCode = currencyCode,
            bufferAmount = bufferAmount,
        )
    }

    private fun systemDefaultTheme(systemDarkMode: Boolean): Theme {
        return if (systemDarkMode) Theme.DARK else Theme.LIGHT
    }
}

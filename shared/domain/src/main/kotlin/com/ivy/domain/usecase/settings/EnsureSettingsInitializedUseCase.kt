package com.ivy.domain.usecase.settings

import com.ivy.data.api.SettingsInitializationStore
import com.ivy.data.model.Theme
import javax.inject.Inject

class EnsureSettingsInitializedUseCase @Inject constructor(
    private val settingsInitializationStore: SettingsInitializationStore
) {
    suspend operator fun invoke(
        systemDarkMode: Boolean,
        baseCurrencyCode: String,
        bufferAmount: Double,
    ) {
        settingsInitializationStore.ensureInitialized(
            defaultTheme = systemDefaultTheme(systemDarkMode),
            baseCurrencyCode = baseCurrencyCode,
            bufferAmount = bufferAmount,
        )
    }

    private fun systemDefaultTheme(systemDarkMode: Boolean): Theme {
        return if (systemDarkMode) Theme.DARK else Theme.LIGHT
    }
}

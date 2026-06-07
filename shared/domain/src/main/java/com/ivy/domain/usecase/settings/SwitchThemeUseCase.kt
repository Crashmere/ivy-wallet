package com.ivy.domain.usecase.settings

import com.ivy.base.theme.Theme
import com.ivy.data.api.SettingsStore
import javax.inject.Inject

class SwitchThemeUseCase @Inject constructor(
    private val settingsStore: SettingsStore
) {
    suspend operator fun invoke(): Theme {
        return settingsStore.setTheme(settingsStore.getTheme().next())
    }

    private fun Theme.next(): Theme = when (this) {
        Theme.LIGHT -> Theme.DARK
        Theme.DARK -> Theme.AMOLED_DARK
        Theme.AMOLED_DARK -> Theme.AUTO
        Theme.AUTO -> Theme.LIGHT
    }
}

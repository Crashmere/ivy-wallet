package com.ivy.domain.usecase.settings

import com.ivy.base.theme.Theme
import com.ivy.data.api.SettingsStore
import javax.inject.Inject

class SwitchThemeUseCase @Inject constructor(
    private val settingsStore: SettingsStore
) {
    suspend operator fun invoke(): Theme {
        return settingsStore.switchTheme()
    }
}

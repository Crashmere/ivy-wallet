package com.ivy.domain.usecase.settings

import com.ivy.base.theme.Theme
import com.ivy.data.repository.SettingsRepository
import javax.inject.Inject

class SwitchThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Theme {
        return settingsRepository.switchTheme()
    }
}

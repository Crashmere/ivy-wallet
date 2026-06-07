package com.ivy.domain.usecase.settings

import com.ivy.base.theme.Theme
import com.ivy.data.repository.LegacySettingsRepository
import javax.inject.Inject

class SwitchThemeUseCase @Inject constructor(
    private val legacySettingsRepository: LegacySettingsRepository
) {
    suspend operator fun invoke(): Theme {
        return legacySettingsRepository.switchTheme()
    }
}

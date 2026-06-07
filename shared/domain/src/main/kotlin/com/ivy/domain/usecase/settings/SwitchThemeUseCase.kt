package com.ivy.domain.usecase.settings

import com.ivy.data.api.ThemeStore
import com.ivy.data.model.Theme
import javax.inject.Inject

class SwitchThemeUseCase @Inject constructor(
    private val themeStore: ThemeStore
) {
    suspend operator fun invoke(): Theme {
        return themeStore.setTheme(themeStore.getTheme().next())
    }

    private fun Theme.next(): Theme = when (this) {
        Theme.LIGHT -> Theme.DARK
        Theme.DARK -> Theme.AMOLED_DARK
        Theme.AMOLED_DARK -> Theme.AUTO
        Theme.AUTO -> Theme.LIGHT
    }
}

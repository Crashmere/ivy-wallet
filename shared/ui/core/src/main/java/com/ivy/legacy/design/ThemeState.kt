package com.ivy.legacy.design

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ivy.base.legacy.Theme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeState @Inject constructor() {
    var theme by mutableStateOf(Theme.LIGHT)
        private set

    fun update(theme: Theme) {
        this.theme = theme
    }
}

@Suppress("CompositionLocalAllowlist")
val LocalThemeState = compositionLocalOf<ThemeState> { error("No LocalThemeState") }

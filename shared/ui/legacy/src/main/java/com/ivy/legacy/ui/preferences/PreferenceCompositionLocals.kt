package com.ivy.legacy.ui.preferences

import androidx.compose.runtime.compositionLocalOf
import com.ivy.domain.preferences.toggles.PreferenceToggleRepository
import com.ivy.domain.preferences.toggles.PreferenceToggles

@Suppress("CompositionLocalAllowlist")
val LocalPreferenceToggleRepository = compositionLocalOf<PreferenceToggleRepository> {
    error("No LocalPreferenceToggleRepository")
}

@Suppress("CompositionLocalAllowlist")
val LocalPreferenceToggles = compositionLocalOf<PreferenceToggles> {
    error("No LocalPreferenceToggles")
}

package com.ivy.legacy.ui.preferences

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ivy.domain.preferences.toggles.PreferenceToggles

@Suppress("CompositionLocalAllowlist")
val LocalPreferenceDataStore = compositionLocalOf<DataStore<Preferences>> {
    error("No LocalPreferenceDataStore")
}

@Suppress("CompositionLocalAllowlist")
val LocalPreferenceToggles = compositionLocalOf<PreferenceToggles> {
    error("No LocalPreferenceToggles")
}

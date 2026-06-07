package com.ivy.domain.preferences.toggles

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

@Suppress("CompositionLocalAllowlist")
val LocalPreferenceDataStore = compositionLocalOf<DataStore<Preferences>> {
    error("No LocalPreferenceDataStore")
}

@Suppress("CompositionLocalAllowlist")
val LocalPreferenceToggles = compositionLocalOf<PreferenceToggles> {
    error("No LocalPreferenceToggles")
}

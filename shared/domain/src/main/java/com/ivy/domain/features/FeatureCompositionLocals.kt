package com.ivy.domain.features

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

@Suppress("CompositionLocalAllowlist")
val LocalFeatureDataStore = compositionLocalOf<DataStore<Preferences>> {
    error("No LocalFeatureDataStore")
}

@Suppress("CompositionLocalAllowlist")
val LocalFeatures = compositionLocalOf<Features> {
    error("No LocalFeatures")
}

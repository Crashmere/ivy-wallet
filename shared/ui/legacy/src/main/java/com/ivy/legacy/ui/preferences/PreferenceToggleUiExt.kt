package com.ivy.legacy.ui.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.ivy.domain.preferences.toggles.BoolPreference

@Composable
fun BoolPreference.asEnabledState(): Boolean {
    val dataStore = LocalPreferenceDataStore.current
    val preferenceValue = remember(dataStore) { enabledFlow(dataStore) }
        .collectAsState(defaultValue).value
    return preferenceValue ?: defaultValue
}

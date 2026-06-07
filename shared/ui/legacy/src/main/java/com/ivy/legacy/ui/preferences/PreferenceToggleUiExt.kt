package com.ivy.legacy.ui.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.ivy.domain.preferences.toggles.BoolPreference

@Composable
fun BoolPreference.asEnabledState(): Boolean {
    val repository = LocalPreferenceToggleRepository.current
    val preferenceValue = remember(repository) { repository.enabledFlow(this) }
        .collectAsState(defaultValue).value
    return preferenceValue ?: defaultValue
}

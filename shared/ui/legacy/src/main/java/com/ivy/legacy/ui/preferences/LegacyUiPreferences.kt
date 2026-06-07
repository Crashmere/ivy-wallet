package com.ivy.legacy.ui.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.Flow

class LegacyUiPreferences(
    val standardKeypadLayout: LegacyBoolPreference,
)

class LegacyBoolPreference(
    val defaultValue: Boolean,
    val enabledFlow: Flow<Boolean?>,
)

@Suppress("CompositionLocalAllowlist")
val LocalLegacyUiPreferences = compositionLocalOf<LegacyUiPreferences> {
    error("No LocalLegacyUiPreferences")
}

@Composable
fun LegacyBoolPreference.asEnabledState(): Boolean {
    return enabledFlow.collectAsState(defaultValue).value ?: defaultValue
}

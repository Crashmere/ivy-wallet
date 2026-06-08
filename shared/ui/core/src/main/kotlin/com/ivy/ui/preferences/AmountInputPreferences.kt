package com.ivy.ui.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.Flow

class AmountInputPreferences(
    val standardKeypadLayout: UiBoolPreference,
)

class UiBoolPreference(
    val defaultValue: Boolean,
    val enabledFlow: Flow<Boolean?>,
)

@Suppress("CompositionLocalAllowlist")
val LocalAmountInputPreferences = compositionLocalOf<AmountInputPreferences> {
    error("No LocalAmountInputPreferences")
}

@Composable
fun UiBoolPreference.asEnabledState(): Boolean {
    return enabledFlow.collectAsState(defaultValue).value ?: defaultValue
}

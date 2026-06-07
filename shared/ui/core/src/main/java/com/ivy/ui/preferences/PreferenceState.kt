package com.ivy.ui.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow

@Composable
fun Flow<Boolean?>.asEnabledState(defaultValue: Boolean): Boolean {
    return collectAsState(defaultValue).value ?: defaultValue
}

package com.ivy.ui.compose

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
fun BackPressHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    val latestOnBack by rememberUpdatedState(onBack)
    BackHandler(enabled = enabled) {
        latestOnBack()
    }
}

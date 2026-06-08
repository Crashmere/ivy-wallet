package com.ivy.ui.compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@SuppressLint("ComposableNaming")
@Composable
fun onCompositionStart(
    cleanUp: () -> Unit = {},
    start: () -> Unit
) {
    val latestStart by rememberUpdatedState(start)
    val latestCleanup by rememberUpdatedState(cleanUp)
    DisposableEffect(Unit) {
        latestStart()
        onDispose { latestCleanup() }
    }
}

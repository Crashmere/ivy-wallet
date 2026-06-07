package com.ivy.ui.time

import androidx.compose.runtime.compositionLocalOf

@Suppress("CompositionLocalAllowlist")
val LocalTimeConverter = compositionLocalOf<TimeConverter> { error("No LocalTimeConverter") }

@Suppress("CompositionLocalAllowlist")
val LocalTimeProvider = compositionLocalOf<TimeProvider> { error("No LocalTimeProvider") }

@Suppress("CompositionLocalAllowlist")
val LocalTimeFormatter = compositionLocalOf<TimeFormatter> { error("No LocalTimeFormatter") }

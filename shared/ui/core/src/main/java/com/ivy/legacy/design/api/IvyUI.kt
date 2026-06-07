package com.ivy.legacy.design.api

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.legacy.design.l0_system.IvyTheme
import com.ivy.ui.platform.DatePicker
import com.ivy.ui.theme.LocalThemeState
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.time.TimeFormatter

@Suppress("CompositionLocalAllowlist")
@Deprecated("Used only for time migration to Instant. Never use it in new code!")
val LocalTimeConverter = compositionLocalOf<TimeConverter> { error("No LocalTimeConverter") }

@Suppress("CompositionLocalAllowlist")
@Deprecated("Used only for time migration to Instant. Never use it in new code!")
val LocalTimeProvider = compositionLocalOf<TimeProvider> { error("No LocalTimeProvider") }

@Suppress("CompositionLocalAllowlist")
@Deprecated("Used only for time migration to Instant. Never use it in new code!")
val LocalTimeFormatter = compositionLocalOf<TimeFormatter> { error("No LocalTimeFormatter") }

@Suppress("CompositionLocalAllowlist")
val LocalDatePicker = compositionLocalOf<DatePicker> { error("No LocalDatePicker") }

@SuppressLint("ComposeModifierMissing")
@Composable
fun IvyUI(
    timeConverter: TimeConverter,
    timeProvider: TimeProvider,
    timeFormatter: TimeFormatter,
    datePicker: DatePicker,
    themeState: ThemeState,
    design: IvyDesign,
    includeSurface: Boolean = true,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    CompositionLocalProvider(
        LocalThemeState provides themeState,
        LocalTimeConverter provides timeConverter,
        LocalTimeProvider provides timeProvider,
        LocalTimeFormatter provides timeFormatter,
        LocalDatePicker provides datePicker,
    ) {
        IvyTheme(
            theme = themeState.theme,
            design = design
        ) {
            WrapWithSurface(includeSurface = includeSurface) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun WrapWithSurface(
    includeSurface: Boolean,
    content: @Composable () -> Unit,
) {
    if (includeSurface) {
        Surface {
            content()
        }
    } else {
        content()
    }
}

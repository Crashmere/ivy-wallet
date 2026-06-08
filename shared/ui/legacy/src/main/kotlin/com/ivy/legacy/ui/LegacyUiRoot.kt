package com.ivy.legacy.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.ivy.legacy.ui.theme.LegacyUiTheme
import com.ivy.legacy.ui.theme.system.LegacyThemeProvider
import com.ivy.ui.platform.DatePicker
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.ui.theme.LocalThemeState
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.time.LocalTimeConverter
import com.ivy.ui.time.LocalTimeFormatter
import com.ivy.ui.time.LocalTimeProvider
import com.ivy.ui.time.TimeConverter
import com.ivy.ui.time.TimeFormatter
import com.ivy.ui.time.TimeProvider

@SuppressLint("ComposeModifierMissing")
@Composable
fun LegacyUiRoot(
    timeConverter: TimeConverter,
    timeProvider: TimeProvider,
    timeFormatter: TimeFormatter,
    datePicker: DatePicker,
    themeState: ThemeState,
    legacyTheme: LegacyUiTheme,
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
        LegacyThemeProvider(
            theme = legacyTheme
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

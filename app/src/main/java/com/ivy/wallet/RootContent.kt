package com.ivy.wallet

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ivy.IvyNavGraph
import com.ivy.base.theme.Theme
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.domain.preferences.toggles.PreferenceToggleRepository
import com.ivy.domain.preferences.toggles.PreferenceToggles
import com.ivy.legacy.ui.preferences.LocalPreferenceToggleRepository
import com.ivy.legacy.ui.preferences.LocalPreferenceToggles
import com.ivy.legacy.ui.state.LocalPeriodState
import com.ivy.legacy.ui.state.PeriodState
import com.ivy.ui.navigation.Navigation
import com.ivy.ui.navigation.NavigationRoot
import com.ivy.legacy.ui.LegacyUiRoot
import com.ivy.ui.platform.BuildInfoProvider
import com.ivy.ui.platform.FileSharer
import com.ivy.ui.platform.LocalBuildInfoProvider
import com.ivy.ui.platform.LocalFileSharer
import com.ivy.ui.theme.IvyMaterial3Theme
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.time.TimeFormatter
import com.ivy.ui.time.impl.DateTimePicker
import com.ivy.wallet.platform.ActivityDatePicker
import com.ivy.wallet.ui.applocked.AppLockedScreen

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun RootContent(
    themeState: ThemeState,
    periodState: PeriodState,
    navigation: Navigation,
    timeConverter: TimeConverter,
    timeProvider: TimeProvider,
    timeFormatter: TimeFormatter,
    dateTimePicker: DateTimePicker,
    datePicker: ActivityDatePicker,
    preferenceToggles: PreferenceToggles,
    preferenceToggleRepository: PreferenceToggleRepository,
    buildInfoProvider: BuildInfoProvider,
    fileSharer: FileSharer,
    viewModel: RootViewModel,
    intent: Intent,
    hasLockScreen: () -> Boolean,
    onShowOSBiometricsModal: () -> Unit,
) {
    CompositionLocalProvider(
        LocalPeriodState provides periodState,
        LocalPreferenceToggles provides preferenceToggles,
        LocalPreferenceToggleRepository provides preferenceToggleRepository,
        LocalBuildInfoProvider provides buildInfoProvider,
        LocalFileSharer provides fileSharer,
    ) {
        val isSystemInDarkTheme = isSystemInDarkTheme()

        LaunchedEffect(isSystemInDarkTheme) {
            viewModel.start(isSystemInDarkTheme, intent)
        }

        val appLocked by viewModel.appLocked.collectAsState()
        when (appLocked) {
            null -> {
            }

            true -> {
                LegacyUiRoot(
                    timeConverter = timeConverter,
                    timeProvider = timeProvider,
                    timeFormatter = timeFormatter,
                    datePicker = datePicker,
                    themeState = themeState,
                ) {
                    AppLockedScreen(
                        hasLockScreen = hasLockScreen,
                        onShowOSBiometricsModal = onShowOSBiometricsModal,
                        onContinueWithoutAuthentication = {
                            viewModel.unlockApp()
                        }
                    )
                }
            }

            false -> {
                NavigationRoot(navigation = navigation) { screen ->
                    LegacyUiRoot(
                        includeSurface = screen?.isLegacy ?: true,
                        timeConverter = timeConverter,
                        timeProvider = timeProvider,
                        timeFormatter = timeFormatter,
                        datePicker = datePicker,
                        themeState = themeState,
                    ) {
                        IvyNavGraph(screen)
                    }
                }
            }
        }

        IvyMaterial3Theme(
            dark = isDarkThemeEnabled(
                theme = themeState.theme,
                systemDarkTheme = isSystemInDarkTheme
            ),
            isTrueBlack = themeState.theme == Theme.AMOLED_DARK
        ) {
            dateTimePicker.Content()
        }
    }
}

private fun isDarkThemeEnabled(theme: Theme, systemDarkTheme: Boolean): Boolean {
    return when (theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.AMOLED_DARK -> true
        else -> systemDarkTheme
    }
}

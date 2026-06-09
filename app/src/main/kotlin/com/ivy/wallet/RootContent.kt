package com.ivy.wallet

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.ivy.data.model.Theme
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.ui.preferences.AmountInputPreferences
import com.ivy.ui.preferences.LocalAmountInputPreferences
import com.ivy.ui.preferences.UiBoolPreference
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.Navigation
import com.ivy.ui.navigation.NavigationRoot
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.period.LocalPeriodState
import com.ivy.ui.period.PeriodState
import com.ivy.ui.platform.BuildInfoProvider
import com.ivy.ui.platform.DatePicker
import com.ivy.ui.platform.FileSharer
import com.ivy.ui.platform.LocalBuildInfoProvider
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.ui.platform.LocalFileSharer
import com.ivy.ui.platform.findActivity
import com.ivy.ui.theme.IvyMaterial3Theme
import com.ivy.ui.theme.LocalThemeState
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.time.DateTimePicker
import com.ivy.ui.time.LocalTimeConverter
import com.ivy.ui.time.LocalTimeFormatter
import com.ivy.ui.time.LocalTimeProvider
import com.ivy.ui.time.TimeConverter
import com.ivy.ui.time.TimeFormatter
import com.ivy.ui.time.TimeProvider
import com.ivy.wallet.navigation.IvyNavGraph
import com.ivy.wallet.ui.applocked.AppLockedScreen

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun RootContent(
    themeState: ThemeState,
    periodState: PeriodState,
    navigation: Navigation,
    timeConverter: TimeConverter,
    timeProvider: TimeProvider,
    timeFormatter: TimeFormatter,
    dateTimePicker: DateTimePicker,
    datePicker: DatePicker,
    preferenceToggles: PreferenceToggleCatalog,
    preferenceToggleService: PreferenceToggleService,
    buildInfoProvider: BuildInfoProvider,
    fileSharer: FileSharer,
    viewModel: RootViewModel,
    addTransactionType: TransactionRouteType?,
    hasLockScreen: () -> Boolean,
    onShowOSBiometricsModal: () -> Unit,
) {
    val amountInputPreferences = remember(preferenceToggles, preferenceToggleService) {
        AmountInputPreferences(
            standardKeypadLayout = UiBoolPreference(
                defaultValue = preferenceToggles.standardKeypadLayout.defaultValue,
                enabledFlow = preferenceToggleService.enabledFlow(
                    preferenceToggles.standardKeypadLayout
                )
            )
        )
    }

    CompositionLocalProvider(
        LocalPeriodState provides periodState,
        LocalAmountInputPreferences provides amountInputPreferences,
        LocalBuildInfoProvider provides buildInfoProvider,
        LocalFileSharer provides fileSharer,
    ) {
        val isSystemInDarkTheme = isSystemInDarkTheme()

        LaunchedEffect(viewModel, navigation) {
            viewModel.events.collect { event ->
                when (event) {
                    RootUiEvent.OpenMain -> navigation.navigateTo(MainScreen)
                    is RootUiEvent.OpenAddTransaction -> {
                        navigation.navigateTo(event.toEditTransactionScreen())
                    }
                }
            }
        }

        LaunchedEffect(isSystemInDarkTheme) {
            viewModel.start(isSystemInDarkTheme, addTransactionType)
        }

        val appLocked by viewModel.appLocked.collectAsState()
        when (appLocked) {
            null -> {
            }

            true -> {
                AppUiRoot(
                    timeConverter = timeConverter,
                    timeProvider = timeProvider,
                    timeFormatter = timeFormatter,
                    datePicker = datePicker,
                    themeState = themeState,
                    systemDarkTheme = isSystemInDarkTheme,
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
                    AppUiRoot(
                        includeSurface = true,
                        timeConverter = timeConverter,
                        timeProvider = timeProvider,
                        timeFormatter = timeFormatter,
                        datePicker = datePicker,
                        themeState = themeState,
                        systemDarkTheme = isSystemInDarkTheme,
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

@SuppressLint("ComposeModifierMissing")
@Composable
private fun AppUiRoot(
    timeConverter: TimeConverter,
    timeProvider: TimeProvider,
    timeFormatter: TimeFormatter,
    datePicker: DatePicker,
    themeState: ThemeState,
    systemDarkTheme: Boolean,
    includeSurface: Boolean = true,
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    val darkTheme = isDarkThemeEnabled(
        theme = themeState.theme,
        systemDarkTheme = systemDarkTheme,
    )

    CompositionLocalProvider(
        LocalThemeState provides themeState,
        LocalTimeConverter provides timeConverter,
        LocalTimeProvider provides timeProvider,
        LocalTimeFormatter provides timeFormatter,
        LocalDatePicker provides datePicker,
    ) {
        val view = LocalView.current
        val activity = view.context.findActivity()
        if (!view.isInEditMode && activity != null) {
            SideEffect {
                val window = activity.window
                window.statusBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    !darkTheme
            }
        }

        IvyMaterial3Theme(
            dark = darkTheme,
            isTrueBlack = themeState.theme == Theme.AMOLED_DARK,
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

private fun isDarkThemeEnabled(theme: Theme, systemDarkTheme: Boolean): Boolean {
    return when (theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.AMOLED_DARK -> true
        else -> systemDarkTheme
    }
}

private fun RootUiEvent.OpenAddTransaction.toEditTransactionScreen(): EditTransactionScreen {
    return EditTransactionScreen(
        initialTransactionId = null,
        type = type
    )
}

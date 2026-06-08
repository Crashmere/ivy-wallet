package com.ivy.wallet

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ivy.IvyNavGraph
import com.ivy.data.model.Theme
import com.ivy.data.model.TransactionType
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggles
import com.ivy.ui.LegacyUiRoot
import com.ivy.ui.preferences.AmountInputPreferences
import com.ivy.ui.preferences.LocalAmountInputPreferences
import com.ivy.ui.preferences.UiBoolPreference
import com.ivy.ui.period.LocalPeriodState
import com.ivy.ui.period.PeriodState
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.Navigation
import com.ivy.ui.navigation.NavigationRoot
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.platform.BuildInfoProvider
import com.ivy.ui.platform.DatePicker
import com.ivy.ui.platform.FileSharer
import com.ivy.ui.platform.LocalBuildInfoProvider
import com.ivy.ui.platform.LocalFileSharer
import com.ivy.ui.theme.IvyMaterial3Theme
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.time.TimeConverter
import com.ivy.ui.time.TimeFormatter
import com.ivy.ui.time.TimeProvider
import com.ivy.ui.time.DateTimePicker
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
    preferenceToggles: PreferenceToggles,
    preferenceToggleService: PreferenceToggleService,
    buildInfoProvider: BuildInfoProvider,
    fileSharer: FileSharer,
    viewModel: RootViewModel,
    addTransactionType: TransactionType?,
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
                        includeSurface = true,
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

private fun RootUiEvent.OpenAddTransaction.toEditTransactionScreen(): EditTransactionScreen {
    return EditTransactionScreen(
        initialTransactionId = null,
        type = TransactionRouteType.valueOf(type.name)
    )
}

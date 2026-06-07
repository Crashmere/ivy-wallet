package com.ivy.wallet

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivy.IvyNavGraph
import com.ivy.base.legacy.Theme
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.ui.platform.RootScreen
import com.ivy.legacy.ui.state.LocalPeriodState
import com.ivy.legacy.ui.state.PeriodState
import com.ivy.navigation.Navigation
import com.ivy.navigation.NavigationRoot
import com.ivy.ui.LegacyUiRoot
import com.ivy.ui.theme.IvyMaterial3Theme
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.time.TimeFormatter
import com.ivy.ui.time.impl.DateTimePicker
import com.ivy.wallet.platform.ActivityDatePicker
import com.ivy.wallet.platform.ActivityResultFilePicker
import com.ivy.wallet.platform.BiometricAuthenticator
import com.ivy.wallet.platform.ExternalIntentLauncher
import com.ivy.wallet.platform.registerActivityResultLaunchers
import com.ivy.wallet.platform.registerMaterialDatePicker
import com.ivy.wallet.ui.applocked.AppLockedScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@Suppress("TooManyFunctions")
class RootActivity : AppCompatActivity(), RootScreen {
    @Inject
    lateinit var themeState: ThemeState

    @Inject
    lateinit var periodState: PeriodState

    @Inject
    lateinit var navigation: Navigation

    @Inject
    lateinit var timeConverter: TimeConverter

    @Inject
    lateinit var timeProvider: TimeProvider

    @Inject
    lateinit var timeFormatter: TimeFormatter

    @Inject
    lateinit var dateTimePicker: DateTimePicker

    @Inject
    lateinit var datePicker: ActivityDatePicker

    @Inject
    lateinit var filePicker: ActivityResultFilePicker

    private val viewModel: RootViewModel by viewModels()
    private val externalIntentLauncher by lazy { ExternalIntentLauncher(this) }
    private val biometricAuthenticator by lazy { BiometricAuthenticator(this) }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setupApp()
        setContent {
            CompositionLocalProvider(LocalPeriodState provides periodState) {
                val viewModel: RootViewModel = viewModel()
                val isSystemInDarkTheme = isSystemInDarkTheme()

                LaunchedEffect(isSystemInDarkTheme) {
                    viewModel.start(isSystemInDarkTheme, intent)
                }

                val appLocked by viewModel.appLocked.collectAsState()
                when (appLocked) {
                    null -> { // display nothing
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
                                onShowOSBiometricsModal = {
                                    authenticateWithOSBiometricsModal(
                                        biometricPromptCallback = viewModel.handleBiometricAuthResult()
                                    )
                                },
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
    }

    private fun setupApp() {
        filePicker.registerActivityResultLaunchers(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        datePicker.registerMaterialDatePicker(supportFragmentManager)
    }

    private fun isDarkThemeEnabled(theme: Theme, systemDarkTheme: Boolean): Boolean {
        return when (theme) {
            Theme.LIGHT -> false
            Theme.DARK -> true
            Theme.AMOLED_DARK -> true
            else -> systemDarkTheme
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (viewModel.isAppLockEnabled() && !hasFocus) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isAppLockEnabled()) {
            viewModel.checkUserInactiveTimeStatus()
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.isAppLockEnabled()) {
            viewModel.startUserInactiveTimeCounter()
        }
    }

    private fun authenticateWithOSBiometricsModal(
        biometricPromptCallback: BiometricPrompt.AuthenticationCallback
    ) {
        biometricAuthenticator.authenticate(biometricPromptCallback)
    }

    override fun onBackPressed() {
        if (viewModel.isAppLocked()) {
            super.onBackPressed()
        } else {
            if (!navigation.onBackPressed()) {
                super.onBackPressed()
            }
        }
    }

    override fun openUrlInBrowser(url: String) {
        externalIntentLauncher.openUrlInBrowser(url)
    }

    override fun openGooglePlayAppPage(appId: String) {
        externalIntentLauncher.openGooglePlayAppPage(appId)
    }

    override fun shareCSVFile(fileUri: Uri) {
        externalIntentLauncher.shareCSVFile(fileUri)
    }

    override fun shareZipFile(fileUri: Uri) {
        externalIntentLauncher.shareZipFile(fileUri)
    }

    override val isDebug: Boolean
        get() = BuildConfig.DEBUG
    override val buildVersionName: String
        get() = BuildConfig.VERSION_NAME
    override val buildVersionCode: Int
        get() = BuildConfig.VERSION_CODE

}

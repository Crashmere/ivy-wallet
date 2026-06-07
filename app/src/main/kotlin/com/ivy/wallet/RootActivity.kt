package com.ivy.wallet

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.view.WindowCompat
import com.ivy.domain.preferences.toggles.PreferenceToggleRepository
import com.ivy.domain.preferences.toggles.PreferenceToggles
import com.ivy.legacy.ui.state.PeriodState
import com.ivy.ui.navigation.Navigation
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.time.TimeConverter
import com.ivy.ui.time.TimeFormatter
import com.ivy.ui.time.TimeProvider
import com.ivy.ui.time.impl.DateTimePicker
import com.ivy.wallet.platform.ActivityDatePicker
import com.ivy.wallet.platform.ActivityFileSharer
import com.ivy.wallet.platform.ActivityResultFilePicker
import com.ivy.wallet.platform.AppBuildInfoProvider
import com.ivy.wallet.platform.BiometricAuthenticator
import com.ivy.wallet.platform.SecureWindowController
import com.ivy.wallet.platform.hasLockScreen as deviceHasLockScreen
import com.ivy.wallet.platform.registerActivityResultLaunchers
import com.ivy.wallet.platform.registerMaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RootActivity : AppCompatActivity() {
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

    @Inject
    lateinit var preferenceToggles: PreferenceToggles

    @Inject
    lateinit var preferenceToggleRepository: PreferenceToggleRepository

    private val viewModel: RootViewModel by viewModels()
    private val activityFileSharer by lazy { ActivityFileSharer(this) }
    private val biometricAuthenticator by lazy { BiometricAuthenticator(this) }
    private val secureWindowController by lazy { SecureWindowController(window) }
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleRootBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setupApp()
        setContent {
            RootContent(
                themeState = themeState,
                periodState = periodState,
                navigation = navigation,
                timeConverter = timeConverter,
                timeProvider = timeProvider,
                timeFormatter = timeFormatter,
                dateTimePicker = dateTimePicker,
                datePicker = datePicker,
                preferenceToggles = preferenceToggles,
                preferenceToggleRepository = preferenceToggleRepository,
                buildInfoProvider = AppBuildInfoProvider,
                fileSharer = activityFileSharer,
                viewModel = viewModel,
                intent = intent,
                hasLockScreen = { deviceHasLockScreen(this) },
                onShowOSBiometricsModal = {
                    authenticateWithOSBiometricsModal(
                        biometricPromptCallback = viewModel.handleBiometricAuthResult()
                    )
                }
            )
        }
    }

    private fun setupApp() {
        filePicker.registerActivityResultLaunchers(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        datePicker.registerMaterialDatePicker(supportFragmentManager)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        secureWindowController.updateForWindowFocus(
            appLockEnabled = viewModel.isAppLockEnabled(),
            hasFocus = hasFocus
        )
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

    private fun handleRootBackPressed() {
        if (!viewModel.isAppLocked() && navigation.handleRootBack()) {
            return
        }
        backPressedCallback.isEnabled = false
        try {
            onBackPressedDispatcher.onBackPressed()
        } finally {
            backPressedCallback.isEnabled = true
        }
    }

}

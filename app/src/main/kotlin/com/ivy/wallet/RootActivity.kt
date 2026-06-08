package com.ivy.wallet

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.ui.period.PeriodState
import com.ivy.ui.navigation.Navigation
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.time.TimeConverter
import com.ivy.ui.time.TimeFormatter
import com.ivy.ui.time.TimeProvider
import com.ivy.ui.time.DateTimePicker
import com.ivy.wallet.platform.ActivityDatePicker
import com.ivy.wallet.platform.ActivityFileSharer
import com.ivy.wallet.platform.ActivityResultFilePicker
import com.ivy.wallet.platform.AppBuildInfoProvider
import com.ivy.wallet.platform.BiometricAuthenticator
import com.ivy.wallet.platform.SecureWindowController
import com.ivy.wallet.platform.hasLockScreen as deviceHasLockScreen
import com.ivy.wallet.platform.registerActivityResultLaunchers
import com.ivy.wallet.platform.registerMaterialDatePicker
import com.ivy.wallet.security.RootAppLockHost
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
    internal lateinit var datePicker: ActivityDatePicker

    @Inject
    internal lateinit var filePicker: ActivityResultFilePicker

    @Inject
    lateinit var preferenceToggles: PreferenceToggleCatalog

    @Inject
    lateinit var preferenceToggleService: PreferenceToggleService

    private val viewModel: RootViewModel by viewModels()
    private val activityFileSharer by lazy { ActivityFileSharer(this) }
    private val appLockHost by lazy {
        RootAppLockHost(
            viewModel = viewModel,
            secureWindowController = SecureWindowController(window),
            biometricAuthenticator = BiometricAuthenticator(this),
        )
    }
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
                preferenceToggleService = preferenceToggleService,
                buildInfoProvider = AppBuildInfoProvider,
                fileSharer = activityFileSharer,
                viewModel = viewModel,
                addTransactionType = intent.readAddTransactionTypeExtra(),
                hasLockScreen = { deviceHasLockScreen(this) },
                onShowOSBiometricsModal = appLockHost::showOSBiometricsModal
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
        appLockHost.onWindowFocusChanged(hasFocus)
    }

    override fun onResume() {
        super.onResume()
        appLockHost.onResume()
    }

    override fun onPause() {
        super.onPause()
        appLockHost.onPause()
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

package com.ivy.wallet

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.domain.features.Features
import com.ivy.legacy.ui.state.PeriodState
import com.ivy.navigation.Navigation
import com.ivy.ui.platform.BuildInfoProvider
import com.ivy.ui.platform.FileSharer
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.time.TimeFormatter
import com.ivy.ui.time.impl.DateTimePicker
import com.ivy.wallet.platform.ActivityDatePicker
import com.ivy.wallet.platform.ActivityFileSharer
import com.ivy.wallet.platform.ActivityResultFilePicker
import com.ivy.wallet.platform.BiometricAuthenticator
import com.ivy.wallet.platform.SecureWindowController
import com.ivy.wallet.platform.registerActivityResultLaunchers
import com.ivy.wallet.platform.registerMaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@Suppress("TooManyFunctions")
class RootActivity : AppCompatActivity(),
    BuildInfoProvider,
    FileSharer {
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
    lateinit var features: Features

    @Inject
    lateinit var featureDataStore: DataStore<Preferences>

    private val viewModel: RootViewModel by viewModels()
    private val activityFileSharer by lazy { ActivityFileSharer(this) }
    private val biometricAuthenticator by lazy { BiometricAuthenticator(this) }
    private val secureWindowController by lazy { SecureWindowController(window) }

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
                features = features,
                featureDataStore = featureDataStore,
                buildInfoProvider = this,
                fileSharer = this,
                viewModel = viewModel,
                intent = intent,
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

    override fun onBackPressed() {
        if (viewModel.isAppLocked()) {
            super.onBackPressed()
        } else {
            if (!navigation.onBackPressed()) {
                super.onBackPressed()
            }
        }
    }

    override fun shareCSVFile(fileUri: Uri) {
        activityFileSharer.shareCSVFile(fileUri)
    }

    override fun shareZipFile(fileUri: Uri) {
        activityFileSharer.shareZipFile(fileUri)
    }

    override val isDebug: Boolean
        get() = BuildConfig.DEBUG
    override val buildVersionName: String
        get() = BuildConfig.VERSION_NAME
    override val buildVersionCode: Int
        get() = BuildConfig.VERSION_CODE

}

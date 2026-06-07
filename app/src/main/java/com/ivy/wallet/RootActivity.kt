package com.ivy.wallet

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.ivy.IvyNavGraph
import com.ivy.base.legacy.Theme
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.ui.theme.ThemeState
import com.ivy.legacy.design.api.IvyUI
import com.ivy.ui.theme.IvyMaterial3Theme
import com.ivy.domain.RootScreen
import com.ivy.legacy.ui.state.LocalPeriodState
import com.ivy.legacy.ui.state.PeriodState
import com.ivy.navigation.Navigation
import com.ivy.navigation.NavigationRoot
import com.ivy.ui.R
import com.ivy.ui.time.TimeFormatter
import com.ivy.ui.time.impl.DateTimePicker
import com.ivy.wallet.platform.ActivityDatePicker
import com.ivy.wallet.platform.ActivityResultFilePicker
import com.ivy.wallet.platform.activityForResultLauncher
import com.ivy.wallet.platform.simpleActivityForResultLauncher
import com.ivy.wallet.ui.applocked.AppLockedScreen
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
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

    private lateinit var createFileLauncher: ActivityResultLauncher<String>
    private lateinit var onFileCreated: (fileUri: Uri) -> Unit

    private lateinit var openFileLauncher: ActivityResultLauncher<Unit>
    private lateinit var onFileOpened: (fileUri: Uri) -> Unit

    private val viewModel: RootViewModel by viewModels()

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
                        IvyUI(
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
                            IvyUI(
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
        setupActivityForResultLaunchers()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setupDatePicker()
    }

    private companion object {
        private const val MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000
    }

    private fun setupDatePicker() {
        datePicker.registerPicker { minDate,
                                    maxDate,
                                    initialDate,
                                    onDatePicked ->
            val picker =
                MaterialDatePicker.Builder.datePicker()
                    .setSelection(
                        if (initialDate != null) {
                            initialDate.toEpochDay() * MILLISECONDS_IN_DAY
                        } else {
                            MaterialDatePicker.todayInUtcMilliseconds()
                        }
                    )
                    .build()
            picker.show(supportFragmentManager, "datePicker")
            picker.addOnPositiveButtonClickListener {
                onDatePicked(LocalDate.ofEpochDay(it / MILLISECONDS_IN_DAY))
            }

            if (minDate != null) {
                picker.addOnCancelListener {
                    onDatePicked(minDate)
                }
            }

            if (maxDate != null) {
                picker.addOnCancelListener {
                    onDatePicked(maxDate)
                }
            }

            if (initialDate != null) {
                picker.addOnCancelListener {
                    onDatePicked(initialDate)
                }
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

    private fun setupActivityForResultLaunchers() {
        createFileLauncher()

        openFileLauncher()
    }

    private fun createFileLauncher() {
        createFileLauncher = activityForResultLauncher(
            createIntent = { _, fileName ->
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/csv"
                    putExtra(Intent.EXTRA_TITLE, fileName)

                    // Optionally, specify a URI for the directory that should be opened in
                    // the system file picker before your app creates the document.
                    putExtra(
                        DocumentsContract.EXTRA_INITIAL_URI,
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            .toURI()
                    )
                }
            }
        ) { _, intent ->
            intent?.data?.also {
                onFileCreated(it)
            }
        }

        filePicker.registerCreateFileLauncher { fileName, onFileCreatedCallback ->
            onFileCreated = onFileCreatedCallback

            createFileLauncher.launch(fileName)
        }
    }

    private fun openFileLauncher() {
        openFileLauncher = simpleActivityForResultLauncher(
            intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
        ) { _, intent ->
            intent?.data?.also {
                onFileOpened(it)
            }
        }

        filePicker.registerOpenFileLauncher { onFileOpenedCallback ->
            onFileOpened = onFileOpenedCallback

            openFileLauncher.launch(Unit)
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
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            biometricPromptCallback
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(
                getString(R.string.authentication_required)
            )
            .setSubtitle(
                getString(R.string.authentication_required_description)
            )
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .setConfirmationRequired(false)
            .build()

        biometricPrompt.authenticate(promptInfo)
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

    @Suppress("TooGenericExceptionCaught", "PrintStackTrace")
    override fun openUrlInBrowser(url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.data = Uri.parse(url)
            startActivity(browserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "No browser app found. Visit manually: $url",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Suppress("SwallowedException")
    override fun openGooglePlayAppPage(appId: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appId")))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appId")
                )
            )
        }
    }

    override fun shareCSVFile(fileUri: Uri) {
        val intent = Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = "text/csv"
            },
            null
        )
        startActivity(intent)
    }

    override fun shareZipFile(fileUri: Uri) {
        val intent = Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = "application/zip"
            },
            null
        )
        startActivity(intent)
    }

    override val isDebug: Boolean
        get() = BuildConfig.DEBUG
    override val buildVersionName: String
        get() = BuildConfig.VERSION_NAME
    override val buildVersionCode: Int
        get() = BuildConfig.VERSION_CODE

}

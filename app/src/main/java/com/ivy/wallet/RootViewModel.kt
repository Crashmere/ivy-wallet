package com.ivy.wallet

import android.content.Intent
import androidx.biometric.BiometricPrompt
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.base.model.TransactionType
import com.ivy.domain.preferences.AppPreferences
import com.ivy.domain.usecase.settings.GetThemeUseCase
import com.ivy.ui.theme.ThemeState
import com.ivy.legacy.ui.state.PeriodState
import com.ivy.base.coroutines.ioThread
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.Navigation
import com.ivy.wallet.notification.reminder.TransactionReminderLogic
import com.ivy.wallet.startup.InitialDataSetup
import com.ivy.wallet.security.AppLockController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList", "TooManyFunctions")
class RootViewModel @Inject constructor(
    private val themeState: ThemeState,
    private val periodState: PeriodState,
    private val nav: Navigation,
    private val getTheme: GetThemeUseCase,
    private val appPreferences: AppPreferences,
    private val appLockController: AppLockController,
    private val transactionReminderLogic: TransactionReminderLogic,
    private val initialDataSetup: InitialDataSetup,
) : ViewModel() {

    companion object {
        const val EXTRA_ADD_TRANSACTION_TYPE = "add_transaction_type_extra"
    }

    val appLocked = appLockController.appLocked

    fun start(systemDarkMode: Boolean, intent: Intent) {
        viewModelScope.launch {

            ioThread {
                val theme = getTheme.withSystemFallback(systemDarkMode)
                themeState.update(theme)

                periodState.initStartDayOfMonth(startDay = appPreferences.startDayOfMonth)
            }

        }

        viewModelScope.launch {

            ioThread {
                appLockController.initialize()

                if (isInitialSetupCompleted()) {
                    navigateOnboardedUser(intent)
                } else {
                    initialDataSetup.setupDefaults(systemDarkMode)
                    navigateOnboardedUser(intent)
                }
            }

        }
    }

    private fun navigateOnboardedUser(intent: Intent) {
        if (!handleSpecialStart(intent)) {
            nav.navigateTo(MainScreen)
            transactionReminderLogic.scheduleReminder()
        }
    }

    @Suppress("SwallowedException")
    private fun handleSpecialStart(intent: Intent): Boolean {
        val addTrnType: TransactionType? = try {
            IntentCompat.getSerializableExtra(
                intent,
                EXTRA_ADD_TRANSACTION_TYPE,
                TransactionType::class.java
            )
                ?: TransactionType.valueOf(intent.getStringExtra(EXTRA_ADD_TRANSACTION_TYPE) ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }

        if (addTrnType != null) {
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = addTrnType
                )
            )

            return true
        }

        return false
    }

    @Suppress("EmptyFunctionBlock")
    fun handleBiometricAuthResult(
        onAuthSuccess: () -> Unit = {}
    ): BiometricPrompt.AuthenticationCallback {
        return appLockController.handleBiometricAuthResult(onAuthSuccess)
    }

    private fun isInitialSetupCompleted(): Boolean {
        return appPreferences.initialSetupCompleted
    }

    // App Lock & UserInactivity --------------------------------------------------------------------
    fun isAppLockEnabled(): Boolean {
        return appLockController.isAppLockEnabled()
    }

    fun isAppLocked(): Boolean {
        return appLockController.isAppLocked()
    }

    fun unlockApp() {
        appLockController.unlockApp()
    }

    fun startUserInactiveTimeCounter() {
        appLockController.startUserInactiveTimeCounter(viewModelScope)
    }

    fun checkUserInactiveTimeStatus() {
        appLockController.checkUserInactiveTimeStatus()
    }
}

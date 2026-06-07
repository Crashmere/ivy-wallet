package com.ivy.wallet

import android.content.Intent
import androidx.biometric.BiometricPrompt
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.TransactionType
import com.ivy.domain.usecase.settings.GetStartDayOfMonthUseCase
import com.ivy.domain.usecase.settings.GetThemeUseCase
import com.ivy.domain.usecase.settings.IsInitialSetupCompletedUseCase
import com.ivy.ui.theme.ThemeState
import com.ivy.legacy.ui.state.PeriodState
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.Navigation
import com.ivy.wallet.notification.reminder.TransactionReminderScheduler
import com.ivy.wallet.startup.InitialDataSetup
import com.ivy.wallet.security.AppLockController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList", "TooManyFunctions")
class RootViewModel @Inject constructor(
    private val themeState: ThemeState,
    private val periodState: PeriodState,
    private val nav: Navigation,
    private val getTheme: GetThemeUseCase,
    private val getStartDayOfMonth: GetStartDayOfMonthUseCase,
    private val isInitialSetupCompleted: IsInitialSetupCompletedUseCase,
    private val appLockController: AppLockController,
    private val transactionReminderScheduler: TransactionReminderScheduler,
    private val initialDataSetup: InitialDataSetup,
) : ViewModel() {

    companion object {
        const val EXTRA_ADD_TRANSACTION_TYPE = "add_transaction_type_extra"
    }

    val appLocked = appLockController.appLocked

    fun start(systemDarkMode: Boolean, intent: Intent) {
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                val theme = getTheme.withSystemFallback(systemDarkMode)
                themeState.update(theme)

                periodState.initStartDayOfMonth(startDay = getStartDayOfMonth())
            }

        }

        viewModelScope.launch {

            withContext(Dispatchers.IO) {
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
            transactionReminderScheduler.scheduleReminder()
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
        return isInitialSetupCompleted()
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

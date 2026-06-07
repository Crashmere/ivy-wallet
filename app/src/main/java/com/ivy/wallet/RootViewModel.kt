package com.ivy.wallet

import android.content.Intent
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.base.legacy.Theme
import com.ivy.base.model.TransactionType
import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.domain.preferences.AppPreferences
import com.ivy.ui.theme.ThemeState
import com.ivy.legacy.ui.state.PeriodState
import com.ivy.base.legacy.ioThread
import com.ivy.navigation.EditTransactionScreen
import com.ivy.navigation.MainScreen
import com.ivy.navigation.Navigation
import com.ivy.wallet.notification.reminder.TransactionReminderLogic
import com.ivy.wallet.domain.startup.InitialDataSetup
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
    private val settingsDao: SettingsDao,
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
                val theme = settingsDao.findAll().firstOrNull()?.theme
                    ?: if (systemDarkMode) Theme.DARK else Theme.LIGHT
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
            intent.getSerializableExtra(EXTRA_ADD_TRANSACTION_TYPE) as? TransactionType
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

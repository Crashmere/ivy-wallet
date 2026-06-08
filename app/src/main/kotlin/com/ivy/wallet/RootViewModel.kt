package com.ivy.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.TransactionType
import com.ivy.domain.usecase.settings.GetStartDayOfMonthUseCase
import com.ivy.domain.usecase.settings.GetThemeUseCase
import com.ivy.domain.usecase.settings.IsInitialSetupCompletedUseCase
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.period.PeriodState
import com.ivy.wallet.notification.reminder.TransactionReminderScheduler
import com.ivy.wallet.startup.InitialDataSetup
import com.ivy.wallet.security.AppLockController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList", "TooManyFunctions")
internal class RootViewModel @Inject constructor(
    private val themeState: ThemeState,
    private val periodState: PeriodState,
    private val getTheme: GetThemeUseCase,
    private val getStartDayOfMonth: GetStartDayOfMonthUseCase,
    private val isInitialSetupCompletedUseCase: IsInitialSetupCompletedUseCase,
    private val appLockController: AppLockController,
    private val transactionReminderScheduler: TransactionReminderScheduler,
    private val initialDataSetup: InitialDataSetup,
) : ViewModel() {

    private val _events = Channel<RootUiEvent>(Channel.BUFFERED)
    internal val events = _events.receiveAsFlow()

    val appLocked = appLockController.appLocked

    fun start(systemDarkMode: Boolean, addTransactionType: TransactionType?) {
        viewModelScope.launch {
            val theme = getTheme.withSystemFallback(systemDarkMode)
            themeState.update(theme)

            periodState.initStartDayOfMonth(startDay = getStartDayOfMonth())
        }

        viewModelScope.launch {
            appLockController.initialize()

            if (!isInitialSetupCompletedUseCase()) {
                initialDataSetup.setupDefaults(systemDarkMode)
            }

            openStartDestination(addTransactionType)
        }
    }

    private suspend fun openStartDestination(addTransactionType: TransactionType?) {
        if (addTransactionType != null) {
            _events.send(RootUiEvent.OpenAddTransaction(addTransactionType))
            return
        }

        transactionReminderScheduler.scheduleReminder()
        _events.send(RootUiEvent.OpenMain)
    }

    fun handleBiometricAuthenticationSucceeded() {
        appLockController.handleBiometricAuthenticationSucceeded()
    }

    fun handleBiometricAuthenticationFailed() {
        appLockController.handleBiometricAuthenticationFailed()
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

internal sealed interface RootUiEvent {
    data object OpenMain : RootUiEvent
    data class OpenAddTransaction(val type: TransactionType) : RootUiEvent
}

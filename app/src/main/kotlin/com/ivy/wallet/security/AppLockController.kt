package com.ivy.wallet.security

import com.ivy.domain.usecase.settings.GetAppLockEnabledPreferenceUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

internal class AppLockController @Inject constructor(
    private val getAppLockEnabledPreference: GetAppLockEnabledPreferenceUseCase,
) {
    private companion object {
        private const val USER_INACTIVITY_TIME_LIMIT = 60
        private const val ONE_SECOND = 1000L
    }

    private var appLockEnabled = false

    private val _appLocked = MutableStateFlow<Boolean?>(null)
    val appLocked = _appLocked.asStateFlow()

    private val userInactiveTime = AtomicLong(0)
    private var userInactiveJob: Job? = null

    fun initialize() {
        appLockEnabled = getAppLockEnabledPreference()
        _appLocked.value = appLockEnabled
    }

    fun isAppLockEnabled(): Boolean {
        return appLockEnabled
    }

    fun isAppLocked(): Boolean {
        return appLocked.value ?: true
    }

    fun lockApp() {
        _appLocked.value = true
    }

    fun unlockApp() {
        _appLocked.value = false
    }

    fun handleBiometricAuthenticationSucceeded() {
        unlockApp()
    }

    fun handleBiometricAuthenticationFailed() = Unit

    fun startUserInactiveTimeCounter(scope: CoroutineScope) {
        if (userInactiveJob != null && userInactiveJob!!.isActive) return

        userInactiveJob = scope.launch(Dispatchers.IO) {
            while (userInactiveTime.get() < USER_INACTIVITY_TIME_LIMIT &&
                userInactiveJob != null && !userInactiveJob?.isCancelled!!
            ) {
                delay(ONE_SECOND)
                userInactiveTime.incrementAndGet()
            }

            if (!isAppLocked()) {
                lockApp()
            }

            cancel()
        }
    }

    fun checkUserInactiveTimeStatus() {
        if (userInactiveTime.get() < USER_INACTIVITY_TIME_LIMIT) {
            if (userInactiveJob != null && userInactiveJob?.isCancelled == false) {
                userInactiveJob?.cancel()
                resetUserInactiveTimer()
            }
        }
    }

    private fun resetUserInactiveTimer() {
        userInactiveTime.set(0)
    }
}

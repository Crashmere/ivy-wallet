package com.ivy.wallet.security

import com.ivy.wallet.RootViewModel
import com.ivy.wallet.platform.BiometricAuthenticator
import com.ivy.wallet.platform.SecureWindowController

internal class RootAppLockHost(
    private val viewModel: RootViewModel,
    private val secureWindowController: SecureWindowController,
    private val biometricAuthenticator: BiometricAuthenticator,
) {
    fun onWindowFocusChanged(hasFocus: Boolean) {
        secureWindowController.updateForWindowFocus(
            appLockEnabled = viewModel.isAppLockEnabled(),
            hasFocus = hasFocus
        )
    }

    fun onResume() {
        if (viewModel.isAppLockEnabled()) {
            viewModel.checkUserInactiveTimeStatus()
        }
    }

    fun onPause() {
        if (viewModel.isAppLockEnabled()) {
            viewModel.startUserInactiveTimeCounter()
        }
    }

    fun showOSBiometricsModal() {
        biometricAuthenticator.authenticate(
            onAuthenticationSucceeded = {
                viewModel.handleBiometricAuthenticationSucceeded()
            },
            onAuthenticationFailed = {
                viewModel.handleBiometricAuthenticationFailed()
            }
        )
    }
}

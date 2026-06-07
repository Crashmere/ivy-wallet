package com.ivy.wallet.platform

import android.view.Window
import android.view.WindowManager

class SecureWindowController(
    private val window: Window
) {
    fun updateForWindowFocus(
        appLockEnabled: Boolean,
        hasFocus: Boolean
    ) {
        if (appLockEnabled && !hasFocus) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

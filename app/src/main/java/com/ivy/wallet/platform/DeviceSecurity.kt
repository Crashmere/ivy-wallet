package com.ivy.wallet.platform

import android.app.KeyguardManager
import android.content.Context

fun hasLockScreen(context: Context): Boolean {
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return keyguardManager.isDeviceSecure
}

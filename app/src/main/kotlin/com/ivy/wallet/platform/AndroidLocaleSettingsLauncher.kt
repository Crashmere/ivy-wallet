package com.ivy.wallet.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.ivy.ui.platform.LocaleSettingsLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class AndroidLocaleSettingsLauncher @Inject constructor(
    @ApplicationContext private val context: Context
) : LocaleSettingsLauncher {
    override val appLocaleSettingsAvailable: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    override fun openAppLocaleSettings() {
        if (!appLocaleSettingsAvailable) {
            return
        }

        val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.applicationContext.startActivity(intent)
    }
}

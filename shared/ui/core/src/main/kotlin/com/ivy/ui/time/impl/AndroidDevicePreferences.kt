package com.ivy.ui.time.impl

import android.content.Context
import android.text.format.DateFormat
import com.ivy.ui.time.DevicePreferences
import java.util.Locale

class AndroidDevicePreferences(
    private val context: Context,
) : DevicePreferences {
    override fun is24HourFormat(): Boolean = DateFormat.is24HourFormat(context)
    override fun locale(): Locale = Locale.getDefault()
}

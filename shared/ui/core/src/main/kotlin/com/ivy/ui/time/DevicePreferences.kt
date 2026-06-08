package com.ivy.ui.time

import java.util.Locale

internal interface DevicePreferences {
    fun is24HourFormat(): Boolean
    fun locale(): Locale
}

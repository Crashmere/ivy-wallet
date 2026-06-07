package com.ivy.domain.usecase.settings

import com.ivy.data.api.SettingsPreferenceStore
import javax.inject.Inject

class SetStartDayOfMonthUseCase @Inject constructor(
    private val appPreferences: SettingsPreferenceStore
) {
    operator fun invoke(startDay: Int): Int? {
        if (startDay !in 1..31) return null

        appPreferences.startDayOfMonth = startDay
        return startDay
    }
}

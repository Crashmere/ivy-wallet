package com.ivy.domain.usecase.settings

import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class SetStartDayOfMonthUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore
) {
    operator fun invoke(startDay: Int): Int? {
        if (startDay !in 1..31) return null

        appPreferences.startDayOfMonth = startDay
        return startDay
    }
}

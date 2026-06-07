package com.ivy.domain.usecase.settings

import com.ivy.domain.preferences.AppPreferences
import javax.inject.Inject

class GetStartDayOfMonthUseCase @Inject constructor(
    private val appPreferences: AppPreferences
) {
    operator fun invoke(): Int {
        return appPreferences.startDayOfMonth
    }
}

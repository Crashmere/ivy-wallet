package com.ivy.domain.usecase.settings

import com.ivy.data.api.SettingsPreferenceStore
import javax.inject.Inject

class GetStartDayOfMonthUseCase @Inject constructor(
    private val appPreferences: SettingsPreferenceStore
) {
    operator fun invoke(): Int {
        return appPreferences.startDayOfMonth
    }
}

package com.ivy.domain.usecase.settings

import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class GetStartDayOfMonthUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore
) {
    operator fun invoke(): Int {
        return appPreferences.startDayOfMonth
    }
}

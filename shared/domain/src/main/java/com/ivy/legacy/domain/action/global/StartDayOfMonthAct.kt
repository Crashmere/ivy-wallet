package com.ivy.legacy.domain.action.global

import com.ivy.domain.preferences.AppPreferences
import com.ivy.frp.action.FPAction
import javax.inject.Inject

class StartDayOfMonthAct @Inject constructor(
    private val appPreferences: AppPreferences
) : FPAction<Unit, Int>() {

    override suspend fun Unit.compose(): suspend () -> Int = suspend {
        appPreferences.startDayOfMonth
    }
}

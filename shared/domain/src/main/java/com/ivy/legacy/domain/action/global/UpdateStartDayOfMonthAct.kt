package com.ivy.legacy.domain.action.global

import com.ivy.domain.preferences.AppPreferences
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.frp.monad.Res
import com.ivy.legacy.frp.monad.thenIfSuccess
import javax.inject.Inject

class UpdateStartDayOfMonthAct @Inject constructor(
    private val appPreferences: AppPreferences
) : FPAction<Int, Res<String, Int>>() {

    override suspend fun Int.compose(): suspend () -> Res<String, Int> = suspend {
        val startDay = this

        if (startDay in 1..31) {
            Res.Ok(startDay)
        } else {
            Res.Err("Invalid start day $startDay. Start date must be between 1 and 31.")
        }
    } thenIfSuccess { startDay ->
        appPreferences.startDayOfMonth = startDay
        Res.Ok(startDay)
    }
}

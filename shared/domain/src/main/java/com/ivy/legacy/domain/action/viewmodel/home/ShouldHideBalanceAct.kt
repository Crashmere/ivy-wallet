package com.ivy.legacy.domain.action.viewmodel.home

import com.ivy.domain.preferences.AppPreferences
import com.ivy.base.frp.action.FPAction
import javax.inject.Inject

class ShouldHideBalanceAct @Inject constructor(
    private val appPreferences: AppPreferences
) : FPAction<Unit, Boolean>() {
    override suspend fun Unit.compose(): suspend () -> Boolean = {
        appPreferences.hideCurrentBalance
    }
}

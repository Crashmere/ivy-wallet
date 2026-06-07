package com.ivy.legacy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ivy.base.legacy.SharedPrefs
import com.ivy.design.IvyContext
import javax.inject.Inject
import javax.inject.Singleton

@Deprecated("Legacy code. Don't use it, please.")
@Singleton
class IvyWalletCtx @Inject constructor() : IvyContext() {
    // ------------------------------------------ State ---------------------------------------------
    @Deprecated("Legacy code. Don't use it, please.")
    var startDayOfMonth = 1
        private set

    @Deprecated("Legacy code. Don't use it, please.")
    fun setStartDayOfMonth(day: Int) {
        startDayOfMonth = day
    }

    @Deprecated("Legacy code. Don't use it, please.")
    fun initStartDayOfMonthInMemory(sharedPrefs: SharedPrefs): Int {
        startDayOfMonth = sharedPrefs.getInt(SharedPrefs.START_DATE_OF_MONTH, 1)
        return startDayOfMonth
    }

    @Deprecated("Legacy code. Don't use it, please.")
    var selectedPeriod: com.ivy.legacy.data.model.TimePeriod =
        com.ivy.legacy.data.model.TimePeriod.currentMonth(
            startDayOfMonth = startDayOfMonth // this is default value
        )

    @Deprecated("Legacy code. Don't use it, please.")
    private var selectedPeriodInitialized = false

    @Deprecated("Legacy code. Don't use it, please.")
    fun initSelectedPeriodInMemory(
        startDayOfMonth: Int,
        forceReinitialize: Boolean = false
    ): com.ivy.legacy.data.model.TimePeriod {
        if (!selectedPeriodInitialized || forceReinitialize) {
            selectedPeriod = com.ivy.legacy.data.model.TimePeriod.currentMonth(
                startDayOfMonth = startDayOfMonth
            )
            selectedPeriodInitialized = true
        }

        return selectedPeriod
    }

    @Deprecated("Legacy code. Don't use it, please.")
    fun updateSelectedPeriodInMemory(period: com.ivy.legacy.data.model.TimePeriod) {
        selectedPeriod = period
    }

    @Deprecated("Legacy code. Don't use it, please.")
    var mainTab by mutableStateOf(com.ivy.legacy.data.model.MainTab.HOME)
        private set

    @Deprecated("Legacy code. Don't use it, please.")
    fun selectMainTab(tab: com.ivy.legacy.data.model.MainTab) {
        mainTab = tab
    }

    @Deprecated("Legacy code. Don't use it, please.")
    var moreMenuExpanded = false
        private set

    @Deprecated("Legacy code. Don't use it, please.")
    fun setMoreMenuExpanded(expanded: Boolean) {
        moreMenuExpanded = expanded
    }
    // ------------------------------------------ State ---------------------------------------------

}

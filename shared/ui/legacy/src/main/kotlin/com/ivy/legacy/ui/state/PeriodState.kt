package com.ivy.legacy.ui.state

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ivy.data.model.FromToTimeRange
import com.ivy.legacy.ui.model.period.TimePeriod
import com.ivy.ui.time.TimeConverter
import com.ivy.ui.time.TimeProvider
import java.time.ZoneOffset

class PeriodState(
    private val timeConverter: TimeConverter,
    private val timeProvider: TimeProvider,
) {
    var startDayOfMonth by mutableIntStateOf(1)
        private set

    var selectedPeriod by mutableStateOf(
        TimePeriod.currentMonth(
            startDayOfMonth = startDayOfMonth,
            timeProvider = timeProvider,
        )
    )
        private set

    private var selectedPeriodInitialized = false

    fun updateStartDayOfMonth(day: Int) {
        startDayOfMonth = day
    }

    fun initStartDayOfMonth(startDay: Int): Int {
        startDayOfMonth = startDay
        return startDayOfMonth
    }

    fun initSelectedPeriod(
        startDayOfMonth: Int = this.startDayOfMonth,
        forceReinitialize: Boolean = false
    ): TimePeriod {
        if (!selectedPeriodInitialized || forceReinitialize) {
            selectedPeriod = TimePeriod.currentMonth(
                startDayOfMonth = startDayOfMonth,
                timeProvider = timeProvider,
            )
            selectedPeriodInitialized = true
        }

        return selectedPeriod
    }

    fun select(period: TimePeriod) {
        selectedPeriod = period
    }

    fun currentMonth(): TimePeriod = TimePeriod.currentMonth(
        startDayOfMonth = startDayOfMonth,
        timeProvider = timeProvider,
    )

    fun rangeOf(period: TimePeriod = selectedPeriod): FromToTimeRange =
        period.toRange(startDayOfMonth, timeConverter, timeProvider)

    fun shiftMonth(period: TimePeriod, increment: Long): TimePeriod? {
        val month = period.month ?: return null
        val year = period.year ?: timeProvider.utcNow().atZone(ZoneOffset.UTC).year
        return month.incrementMonthPeriod(
            increment = increment,
            year = year,
            referenceDate = timeProvider.localDateNow(),
        )
    }
}

@Suppress("CompositionLocalAllowlist")
val LocalPeriodState = compositionLocalOf<PeriodState> { error("No LocalPeriodState") }

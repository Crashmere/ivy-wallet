package com.ivy.domain.usecase.settings

import com.ivy.data.api.StartDayOfMonthStore
import javax.inject.Inject

class SetStartDayOfMonthUseCase @Inject internal constructor(
    private val startDayOfMonthStore: StartDayOfMonthStore
) {
    operator fun invoke(startDay: Int): Int? {
        if (startDay !in 1..31) return null

        startDayOfMonthStore.startDayOfMonth = startDay
        return startDay
    }
}

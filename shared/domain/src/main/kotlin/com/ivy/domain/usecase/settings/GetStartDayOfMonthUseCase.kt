package com.ivy.domain.usecase.settings

import com.ivy.data.api.StartDayOfMonthStore
import javax.inject.Inject

class GetStartDayOfMonthUseCase @Inject internal constructor(
    private val startDayOfMonthStore: StartDayOfMonthStore
) {
    operator fun invoke(): Int {
        return startDayOfMonthStore.startDayOfMonth
    }
}

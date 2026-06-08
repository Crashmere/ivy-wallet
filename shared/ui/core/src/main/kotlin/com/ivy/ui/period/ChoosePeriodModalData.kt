package com.ivy.ui.period

import java.util.UUID

data class ChoosePeriodModalData(
    val id: UUID = UUID.randomUUID(),
    val period: TimePeriod
)

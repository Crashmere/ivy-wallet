package com.ivy.legacy.ui.modal

import com.ivy.ui.period.TimePeriod
import java.util.UUID

data class ChoosePeriodModalData(
    val id: UUID = UUID.randomUUID(),
    val period: TimePeriod
)

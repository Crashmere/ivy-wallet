package com.ivy.ui.modal

import com.ivy.data.model.IntervalType
import java.time.LocalDateTime
import java.util.UUID

data class RecurringRuleModalData(
    val initialStartDate: LocalDateTime?,
    val initialIntervalN: Int?,
    val initialIntervalType: IntervalType?,
    val initialOneTime: Boolean = false,
    val id: UUID = UUID.randomUUID()
)

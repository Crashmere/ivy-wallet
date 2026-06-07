package com.ivy.data.model.legacy

import com.ivy.data.model.TransactionType
import com.ivy.data.model.IntervalType
import java.time.Instant
import java.util.UUID

data class PlannedPaymentRule(
    val startDate: Instant?,
    val intervalN: Int?,
    val intervalType: IntervalType?,
    val oneTime: Boolean,

    val type: TransactionType,
    val accountId: UUID,
    val amount: Double = 0.0,
    val categoryId: UUID? = null,
    val title: String? = null,
    val description: String? = null,

    val isDeleted: Boolean = false,

    val id: UUID = UUID.randomUUID()
)

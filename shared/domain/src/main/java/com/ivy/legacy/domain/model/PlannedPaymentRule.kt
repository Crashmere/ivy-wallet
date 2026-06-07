package com.ivy.legacy.domain.model

import com.ivy.base.model.TransactionType
import com.ivy.data.db.entity.PlannedPaymentRuleEntity
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
) {
    fun toEntity(): PlannedPaymentRuleEntity = PlannedPaymentRuleEntity(
        startDate = startDate,
        intervalN = intervalN,
        intervalType = intervalType,
        oneTime = oneTime,
        type = type,
        accountId = accountId,
        amount = amount,
        categoryId = categoryId,
        title = title,
        description = description,
        isDeleted = isDeleted,
        id = id
    )
}

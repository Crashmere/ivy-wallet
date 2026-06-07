package com.ivy.data.api

import com.ivy.data.model.PlannedPaymentRule
import java.util.UUID

interface PlannedPaymentRuleStore {
    suspend fun findAll(): List<PlannedPaymentRule>

    suspend fun findAllByOneTime(oneTime: Boolean): List<PlannedPaymentRule>

    suspend fun findById(id: UUID): PlannedPaymentRule?

    suspend fun countPlannedPayments(): Long

    suspend fun save(value: PlannedPaymentRule)

    suspend fun saveMany(values: List<PlannedPaymentRule>)

    suspend fun deleteByAccountId(accountId: UUID)

    suspend fun deleteById(id: UUID)

    suspend fun deleteAll()
}

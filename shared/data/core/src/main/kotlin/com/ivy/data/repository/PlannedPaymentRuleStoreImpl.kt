package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.db.entity.PlannedPaymentRuleEntity
import com.ivy.data.model.legacy.PlannedPaymentRule
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class PlannedPaymentRuleStoreImpl @Inject constructor(
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
    private val dispatchers: DispatchersProvider
) : PlannedPaymentRuleStore {
    override suspend fun findAll(): List<PlannedPaymentRule> = withContext(dispatchers.io) {
        plannedPaymentRuleDao.findAll().map { it.toLegacyModel() }
    }

    override suspend fun findAllByOneTime(
        oneTime: Boolean
    ): List<PlannedPaymentRule> = withContext(dispatchers.io) {
        plannedPaymentRuleDao.findAllByOneTime(oneTime).map { it.toLegacyModel() }
    }

    override suspend fun findById(id: UUID): PlannedPaymentRule? = withContext(dispatchers.io) {
        plannedPaymentRuleDao.findById(id)?.toLegacyModel()
    }

    override suspend fun countPlannedPayments(): Long = withContext(dispatchers.io) {
        plannedPaymentRuleDao.countPlannedPayments()
    }

    override suspend fun save(value: PlannedPaymentRule) {
        withContext(dispatchers.io) {
            plannedPaymentRuleWriter.save(value.toEntity())
        }
    }

    override suspend fun saveMany(values: List<PlannedPaymentRule>) {
        withContext(dispatchers.io) {
            plannedPaymentRuleWriter.saveMany(values.map { it.toEntity() })
        }
    }

    override suspend fun deleteByAccountId(accountId: UUID) {
        withContext(dispatchers.io) {
            plannedPaymentRuleWriter.deletedByAccountId(accountId)
        }
    }

    override suspend fun deleteById(id: UUID) {
        withContext(dispatchers.io) {
            plannedPaymentRuleWriter.deleteById(id)
        }
    }

    override suspend fun deleteAll() {
        withContext(dispatchers.io) {
            plannedPaymentRuleWriter.deleteAll()
        }
    }

    private fun PlannedPaymentRuleEntity.toLegacyModel(): PlannedPaymentRule = PlannedPaymentRule(
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

    private fun PlannedPaymentRule.toEntity(): PlannedPaymentRuleEntity = PlannedPaymentRuleEntity(
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

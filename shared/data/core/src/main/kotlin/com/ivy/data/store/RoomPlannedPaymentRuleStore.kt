package com.ivy.data.store

import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.db.entity.PlannedPaymentRuleEntity
import com.ivy.data.model.PlannedPaymentRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class RoomPlannedPaymentRuleStore @Inject constructor(
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
) : PlannedPaymentRuleStore {
    override suspend fun findAll(): List<PlannedPaymentRule> = withContext(Dispatchers.IO) {
        plannedPaymentRuleDao.findAll().map { it.toLegacyModel() }
    }

    override suspend fun findAllByOneTime(
        oneTime: Boolean
    ): List<PlannedPaymentRule> = withContext(Dispatchers.IO) {
        plannedPaymentRuleDao.findAllByOneTime(oneTime).map { it.toLegacyModel() }
    }

    override suspend fun findById(id: UUID): PlannedPaymentRule? = withContext(Dispatchers.IO) {
        plannedPaymentRuleDao.findById(id)?.toLegacyModel()
    }

    override suspend fun countPlannedPayments(): Long = withContext(Dispatchers.IO) {
        plannedPaymentRuleDao.countPlannedPayments()
    }

    override suspend fun save(value: PlannedPaymentRule) {
        withContext(Dispatchers.IO) {
            plannedPaymentRuleWriter.save(value.toEntity())
        }
    }

    override suspend fun saveMany(values: List<PlannedPaymentRule>) {
        withContext(Dispatchers.IO) {
            plannedPaymentRuleWriter.saveMany(values.map { it.toEntity() })
        }
    }

    override suspend fun deleteByAccountId(accountId: UUID) {
        withContext(Dispatchers.IO) {
            plannedPaymentRuleWriter.deletedByAccountId(accountId)
        }
    }

    override suspend fun deleteById(id: UUID) {
        withContext(Dispatchers.IO) {
            plannedPaymentRuleWriter.deleteById(id)
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
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

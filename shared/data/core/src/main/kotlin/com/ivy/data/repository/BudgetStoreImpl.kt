package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.BudgetStore
import com.ivy.data.db.dao.read.BudgetDao
import com.ivy.data.db.dao.write.WriteBudgetDao
import com.ivy.data.db.entity.BudgetEntity
import com.ivy.data.model.legacy.Budget
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class BudgetStoreImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val budgetWriter: WriteBudgetDao,
    private val dispatchers: DispatchersProvider
) : BudgetStore {
    override suspend fun findAll(): List<Budget> = withContext(dispatchers.io) {
        budgetDao.findAll().map { it.toLegacyModel() }
    }

    override suspend fun findById(id: UUID): Budget? = withContext(dispatchers.io) {
        budgetDao.findById(id)?.toLegacyModel()
    }

    override suspend fun findMaxOrderNum(): Double = withContext(dispatchers.io) {
        budgetDao.findMaxOrderNum() ?: 0.0
    }

    override suspend fun save(value: Budget) {
        withContext(dispatchers.io) {
            budgetWriter.save(value.toEntity())
        }
    }

    override suspend fun saveMany(values: List<Budget>) {
        withContext(dispatchers.io) {
            budgetWriter.saveMany(values.map { it.toEntity() })
        }
    }

    override suspend fun deleteById(id: UUID) {
        withContext(dispatchers.io) {
            budgetWriter.deleteById(id)
        }
    }

    override suspend fun deleteAll() {
        withContext(dispatchers.io) {
            budgetWriter.deleteAll()
        }
    }

    private fun BudgetEntity.toLegacyModel(): Budget = Budget(
        name = name,
        amount = amount,
        categoryIdsSerialized = categoryIdsSerialized,
        accountIdsSerialized = accountIdsSerialized,
        isDeleted = isDeleted,
        orderId = orderId,
        id = id
    )

    private fun Budget.toEntity(): BudgetEntity = BudgetEntity(
        name = name,
        amount = amount,
        categoryIdsSerialized = categoryIdsSerialized,
        accountIdsSerialized = accountIdsSerialized,
        isDeleted = isDeleted,
        orderId = orderId,
        id = id,
    )
}

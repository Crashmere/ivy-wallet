package com.ivy.data.repository

import com.ivy.data.api.BudgetStore
import com.ivy.data.db.dao.read.BudgetDao
import com.ivy.data.db.dao.write.WriteBudgetDao
import com.ivy.data.db.entity.BudgetEntity
import com.ivy.data.model.legacy.Budget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class BudgetStoreImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val budgetWriter: WriteBudgetDao,
) : BudgetStore {
    override suspend fun findAll(): List<Budget> = withContext(Dispatchers.IO) {
        budgetDao.findAll().map { it.toLegacyModel() }
    }

    override suspend fun findById(id: UUID): Budget? = withContext(Dispatchers.IO) {
        budgetDao.findById(id)?.toLegacyModel()
    }

    override suspend fun findMaxOrderNum(): Double = withContext(Dispatchers.IO) {
        budgetDao.findMaxOrderNum() ?: 0.0
    }

    override suspend fun save(value: Budget) {
        withContext(Dispatchers.IO) {
            budgetWriter.save(value.toEntity())
        }
    }

    override suspend fun saveMany(values: List<Budget>) {
        withContext(Dispatchers.IO) {
            budgetWriter.saveMany(values.map { it.toEntity() })
        }
    }

    override suspend fun deleteById(id: UUID) {
        withContext(Dispatchers.IO) {
            budgetWriter.deleteById(id)
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
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

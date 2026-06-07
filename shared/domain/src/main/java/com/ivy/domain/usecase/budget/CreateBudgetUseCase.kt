package com.ivy.domain.usecase.budget

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.BudgetDao
import com.ivy.data.db.dao.write.WriteBudgetDao
import com.ivy.data.model.legacy.Budget
import com.ivy.data.model.legacy.CreateBudgetData
import com.ivy.domain.mapper.legacy.toEntity
import com.ivy.legacy.domain.pure.util.nextOrderNum
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateBudgetUseCase @Inject constructor(
    private val budgetDao: BudgetDao,
    private val budgetWriter: WriteBudgetDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(data: CreateBudgetData): Budget? {
        val name = data.name
        if (name.isBlank()) return null
        if (data.amount <= 0) return null

        return try {
            withContext(dispatchers.io) {
                val budget = Budget(
                    name = name.trim(),
                    amount = data.amount,
                    categoryIdsSerialized = data.categoryIdsSerialized,
                    accountIdsSerialized = data.accountIdsSerialized,
                    orderId = budgetDao.findMaxOrderNum().nextOrderNum(),
                )

                budgetWriter.save(budget.toEntity())
                budget
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

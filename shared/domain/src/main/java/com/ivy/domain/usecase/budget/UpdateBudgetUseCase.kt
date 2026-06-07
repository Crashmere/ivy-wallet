package com.ivy.domain.usecase.budget

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.write.WriteBudgetDao
import com.ivy.data.model.legacy.Budget
import com.ivy.legacy.domain.mapper.toEntity
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateBudgetUseCase @Inject constructor(
    private val budgetWriter: WriteBudgetDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(budget: Budget): Boolean {
        if (budget.name.isBlank()) return false
        if (budget.amount <= 0.0) return false

        return try {
            withContext(dispatchers.io) {
                budgetWriter.save(budget.toEntity())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

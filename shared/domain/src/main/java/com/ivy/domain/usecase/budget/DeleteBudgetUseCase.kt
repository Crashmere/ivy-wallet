package com.ivy.domain.usecase.budget

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.write.WriteBudgetDao
import com.ivy.data.model.legacy.Budget
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteBudgetUseCase @Inject constructor(
    private val budgetWriter: WriteBudgetDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(budget: Budget): Boolean {
        return try {
            withContext(dispatchers.io) {
                budgetWriter.deleteById(budget.id)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

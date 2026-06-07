package com.ivy.domain.usecase.budget

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.write.WriteBudgetDao
import com.ivy.legacy.domain.model.Budget
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReorderBudgetsUseCase @Inject constructor(
    private val budgetWriter: WriteBudgetDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(budgets: List<Budget>) {
        withContext(dispatchers.io) {
            budgets.forEachIndexed { index, budget ->
                budgetWriter.save(
                    budget.toEntity().copy(
                        orderId = index.toDouble()
                    )
                )
            }
        }
    }
}

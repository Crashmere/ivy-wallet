package com.ivy.domain.usecase.budget

import com.ivy.data.api.BudgetStore
import com.ivy.data.model.Budget
import javax.inject.Inject

class ReorderBudgetsUseCase @Inject constructor(
    private val budgetStore: BudgetStore,
) {
    suspend operator fun invoke(budgets: List<Budget>) {
        budgetStore.saveMany(
            budgets.mapIndexed { index, budget ->
                budget.copy(orderId = index.toDouble())
            }
        )
    }
}

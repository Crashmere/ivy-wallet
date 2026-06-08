package com.ivy.domain.usecase.budget

import com.ivy.data.api.BudgetStore
import com.ivy.data.model.Budget
import javax.inject.Inject

class UpdateBudgetUseCase @Inject internal constructor(
    private val budgetStore: BudgetStore,
) {
    suspend operator fun invoke(budget: Budget): Boolean {
        if (budget.name.isBlank()) return false
        if (budget.amount <= 0.0) return false

        return try {
            budgetStore.save(budget)
            true
        } catch (_: Exception) {
            false
        }
    }
}

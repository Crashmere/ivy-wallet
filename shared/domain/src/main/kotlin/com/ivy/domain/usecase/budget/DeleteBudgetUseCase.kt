package com.ivy.domain.usecase.budget

import com.ivy.data.api.BudgetStore
import com.ivy.data.model.Budget
import javax.inject.Inject

class DeleteBudgetUseCase @Inject internal constructor(
    private val budgetStore: BudgetStore,
) {
    suspend operator fun invoke(budget: Budget): Boolean {
        return try {
            budgetStore.deleteById(budget.id)
            true
        } catch (_: Exception) {
            false
        }
    }
}

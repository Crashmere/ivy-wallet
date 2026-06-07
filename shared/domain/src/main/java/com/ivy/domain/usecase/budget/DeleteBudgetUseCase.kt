package com.ivy.domain.usecase.budget

import com.ivy.data.api.BudgetStore
import com.ivy.data.model.legacy.Budget
import javax.inject.Inject

class DeleteBudgetUseCase @Inject constructor(
    private val budgetStore: BudgetStore,
) {
    suspend operator fun invoke(budget: Budget): Boolean {
        return try {
            budgetStore.deleteById(budget.id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

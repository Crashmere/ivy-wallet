package com.ivy.domain.usecase.budget

import com.ivy.data.api.BudgetStore
import com.ivy.data.model.legacy.Budget
import javax.inject.Inject

class GetBudgetsUseCase @Inject constructor(
    private val budgetStore: BudgetStore,
) {
    suspend operator fun invoke(): List<Budget> {
        return budgetStore.findAll()
    }
}

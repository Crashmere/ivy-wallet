package com.ivy.domain.usecase.budget

import com.ivy.data.api.BudgetStore
import com.ivy.data.model.Budget
import javax.inject.Inject

class GetBudgetsUseCase @Inject internal constructor(
    private val budgetStore: BudgetStore,
) {
    suspend operator fun invoke(): List<Budget> {
        return budgetStore.findAll()
    }
}

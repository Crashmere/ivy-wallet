package com.ivy.domain.usecase.budget

import com.ivy.data.api.BudgetStore
import com.ivy.data.model.legacy.Budget
import com.ivy.data.model.legacy.CreateBudgetData
import com.ivy.domain.util.nextOrderNum
import javax.inject.Inject

class CreateBudgetUseCase @Inject constructor(
    private val budgetStore: BudgetStore,
) {
    suspend operator fun invoke(data: CreateBudgetData): Budget? {
        val name = data.name
        if (name.isBlank()) return null
        if (data.amount <= 0) return null

        return try {
            val budget = Budget(
                name = name.trim(),
                amount = data.amount,
                categoryIdsSerialized = data.categoryIdsSerialized,
                accountIdsSerialized = data.accountIdsSerialized,
                orderId = budgetStore.findMaxOrderNum().nextOrderNum(),
            )

            budgetStore.save(budget)
            budget
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

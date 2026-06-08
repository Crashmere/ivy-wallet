package com.ivy.budgets

import com.ivy.data.model.Budget
import com.ivy.data.model.CreateBudgetData
import java.util.UUID

internal sealed interface BudgetScreenEvent {
    data class OnReorder(val budgetIds: List<UUID>) : BudgetScreenEvent
    data class OnCreateBudget(val budgetData: CreateBudgetData) : BudgetScreenEvent
    data class OnEditBudget(val budget: Budget) : BudgetScreenEvent
    data class OnDeleteBudget(val budgetId: UUID) : BudgetScreenEvent
    data class OnReorderModalVisible(val visible: Boolean) : BudgetScreenEvent
    data class OnBudgetModalData(val budgetModalData: BudgetModalData?) : BudgetScreenEvent
}

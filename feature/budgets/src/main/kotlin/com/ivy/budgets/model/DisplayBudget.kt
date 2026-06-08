package com.ivy.budgets.model

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Budget
import com.ivy.legacy.ui.component.ReorderableItem

@Immutable
internal data class DisplayBudget(
    val budget: Budget,
    val spentAmount: Double
) : ReorderableItem {
    override val orderNum: Double
        get() = budget.orderId

    override fun withNewOrderNum(newOrderNum: Double): ReorderableItem {
        return this.copy(
            budget = budget.copy(
                orderId = newOrderNum
            )
        )
    }
}

package com.ivy.budgets.model

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Reorderable
import com.ivy.data.model.Budget

@Immutable
data class DisplayBudget(
    val budget: Budget,
    val spentAmount: Double
) : Reorderable {
    override val orderNum: Double
        get() = budget.orderId

    override fun withNewOrderNum(newOrderNum: Double): Reorderable {
        return this.copy(
            budget = budget.copy(
                orderId = newOrderNum
            )
        )
    }
}

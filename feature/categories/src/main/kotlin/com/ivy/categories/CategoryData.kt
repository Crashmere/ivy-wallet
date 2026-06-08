package com.ivy.categories

import com.ivy.data.model.Category
import com.ivy.legacy.ui.component.ReorderableItem

internal data class CategoryData(
    val category: Category,
    val monthlyBalance: Double,
    val monthlyExpenses: Double,
    val monthlyIncome: Double
) : ReorderableItem {
    override val orderNum: Double
        get() = category.orderNum

    override fun withNewOrderNum(newOrderNum: Double) = this.copy(
        category = category.copy(
            orderNum = newOrderNum
        )
    )
}

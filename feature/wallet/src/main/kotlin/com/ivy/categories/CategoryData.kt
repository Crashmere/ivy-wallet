package com.ivy.categories

import com.ivy.data.model.Category

internal data class CategoryData(
    val category: Category,
    val monthlyBalance: Double,
    val monthlyExpenses: Double,
    val monthlyIncome: Double
)

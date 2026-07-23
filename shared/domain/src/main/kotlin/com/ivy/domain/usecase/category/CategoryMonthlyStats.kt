package com.ivy.domain.usecase.category

import com.ivy.data.model.Category

data class CategoryMonthlyStats(
    val category: Category,
    val balance: Double,
    val income: Double,
    val expenses: Double,
    val count: Int,
)

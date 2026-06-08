package com.ivy.domain.usecase.budget

import com.ivy.data.model.Budget

data class BudgetSpentAmount(
    val budget: Budget,
    val spentAmount: Double,
)

package com.ivy.budgets.model

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Budget

@Immutable
internal data class DisplayBudget(
    val budget: Budget,
    val spentAmount: Double
)

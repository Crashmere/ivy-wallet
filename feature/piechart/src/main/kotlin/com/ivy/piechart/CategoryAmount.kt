package com.ivy.piechart

import androidx.compose.runtime.Immutable
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.Category

@Immutable
data class CategoryAmount(
    val category: Category?,
    val amount: Double,
    val associatedTransactions: List<LegacyTransaction> = emptyList(),
    val isCategoryUnspecified: Boolean = false
)

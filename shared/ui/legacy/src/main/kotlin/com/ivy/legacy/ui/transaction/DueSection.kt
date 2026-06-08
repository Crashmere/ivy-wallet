package com.ivy.legacy.ui.transaction

import androidx.compose.runtime.Immutable
import com.ivy.data.model.IncomeExpensePair
import com.ivy.data.model.Transaction

@Immutable
data class DueSection(
    val transactions: List<Transaction>,
    val expanded: Boolean,
    val stats: IncomeExpensePair
)

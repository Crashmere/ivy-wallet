package com.ivy.legacy.ui.transaction

import androidx.compose.runtime.Immutable
import com.ivy.data.model.IncomeExpensePair
import com.ivy.data.model.Transaction
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class DueSection(
    val transactions: ImmutableList<Transaction>,
    val expanded: Boolean,
    val stats: IncomeExpensePair
)

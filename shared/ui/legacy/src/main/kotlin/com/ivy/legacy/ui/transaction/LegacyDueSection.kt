package com.ivy.legacy.ui.transaction

import androidx.compose.runtime.Immutable
import com.ivy.data.model.IncomeExpensePair
import com.ivy.data.model.legacy.LegacyTransaction
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class LegacyDueSection(
    val transactions: ImmutableList<LegacyTransaction>,
    val expanded: Boolean,
    val stats: IncomeExpensePair
)

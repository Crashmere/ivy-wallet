package com.ivy.legacy.ui.model

import androidx.compose.runtime.Immutable
import com.ivy.data.model.legacy.Transaction
import com.ivy.data.model.legacy.IncomeExpensePair
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class LegacyDueSection(
    val trns: ImmutableList<Transaction>,
    val expanded: Boolean,
    val stats: IncomeExpensePair
)

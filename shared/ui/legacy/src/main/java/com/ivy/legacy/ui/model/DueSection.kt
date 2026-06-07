package com.ivy.legacy.ui.model

import androidx.compose.runtime.Immutable
import com.ivy.base.model.legacy.Transaction
import com.ivy.legacy.domain.pure.data.IncomeExpensePair
import kotlinx.collections.immutable.ImmutableList

@Deprecated("Uses legacy Transaction")
@Immutable
data class LegacyDueSection(
    val trns: ImmutableList<Transaction>,
    val expanded: Boolean,
    val stats: IncomeExpensePair
)

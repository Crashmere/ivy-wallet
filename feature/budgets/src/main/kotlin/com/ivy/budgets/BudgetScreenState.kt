package com.ivy.budgets

import com.ivy.budgets.model.DisplayBudget
import com.ivy.data.model.Category
import com.ivy.data.model.FromToTimeRange
import com.ivy.data.model.legacy.LegacyAccount
import kotlinx.collections.immutable.ImmutableList
import javax.annotation.concurrent.Immutable

@Immutable
internal data class BudgetScreenState(
    val baseCurrency: String,
    val budgets: ImmutableList<DisplayBudget>,
    val categories: ImmutableList<Category>,
    val accounts: ImmutableList<LegacyAccount>,
    val categoryBudgetsTotal: Double,
    val appBudgetMax: Double,
    val totalRemainingBudgetText: String?,
    val timeRange: FromToTimeRange?,
    val reorderModalVisible: Boolean,
    val budgetModalData: BudgetModalData?
)

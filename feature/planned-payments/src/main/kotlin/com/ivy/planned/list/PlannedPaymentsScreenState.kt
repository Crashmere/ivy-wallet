package com.ivy.planned.list

import com.ivy.data.model.Category
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.PlannedPaymentRule
import kotlinx.collections.immutable.ImmutableList
import javax.annotation.concurrent.Immutable

@Immutable
internal data class PlannedPaymentsScreenState(
    val currency: String,
    val categories: ImmutableList<Category>,
    val accounts: ImmutableList<LegacyAccount>,
    val oneTimePlannedPayment: ImmutableList<PlannedPaymentRule>,
    val oneTimeIncome: Double,
    val oneTimeExpenses: Double,
    val recurringPlannedPayment: ImmutableList<PlannedPaymentRule>,
    val recurringIncome: Double,
    val recurringExpenses: Double,
    val isOneTimePaymentsExpanded: Boolean,
    val isRecurringPaymentsExpanded: Boolean
)

package com.ivy.planned.list

import com.ivy.data.model.PlannedPaymentRule
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID
import javax.annotation.concurrent.Immutable

@Immutable
internal data class PlannedPaymentsScreenState(
    val currency: String,
    val categories: ImmutableList<PlannedPaymentCategory>,
    val accounts: ImmutableList<PlannedPaymentAccount>,
    val oneTimePlannedPayment: ImmutableList<PlannedPaymentRule>,
    val oneTimeIncome: Double,
    val oneTimeExpenses: Double,
    val recurringPlannedPayment: ImmutableList<PlannedPaymentRule>,
    val recurringIncome: Double,
    val recurringExpenses: Double,
    val isOneTimePaymentsExpanded: Boolean,
    val isRecurringPaymentsExpanded: Boolean
)

@Immutable
internal data class PlannedPaymentCategory(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
)

@Immutable
internal data class PlannedPaymentAccount(
    val id: UUID,
    val name: String,
    val icon: String?,
    val currency: String?,
)

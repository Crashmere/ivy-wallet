package com.ivy.balance

import androidx.compose.runtime.Immutable
import com.ivy.legacy.ui.model.period.TimePeriod

@Immutable
data class BalanceState(
    val period: TimePeriod,
    val baseCurrencyCode: String,
    val currentBalance: Double,
    val plannedPaymentsAmount: Double,
    val balanceAfterPlannedPayments: Double
)

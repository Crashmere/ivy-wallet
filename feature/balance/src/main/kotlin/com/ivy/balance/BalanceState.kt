package com.ivy.balance

import androidx.compose.runtime.Immutable
import com.ivy.ui.period.TimePeriod

@Immutable
internal data class BalanceState(
    val period: TimePeriod,
    val baseCurrencyCode: String,
    val currentBalance: Double,
    val plannedPaymentsAmount: Double,
    val balanceAfterPlannedPayments: Double
)

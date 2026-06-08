package com.ivy.loans.loan

import com.ivy.loans.model.DisplayLoan
import com.ivy.loans.model.LoanAccount
import kotlinx.collections.immutable.ImmutableList
import java.time.Instant
import java.util.UUID

internal data class LoanScreenState(
    val baseCurrency: String,
    val completedLoans: ImmutableList<DisplayLoan>,
    val pendingLoans: ImmutableList<DisplayLoan>,
    val accounts: ImmutableList<LoanAccount>,
    val selectedAccountId: UUID?,
    val reorderModalVisible: Boolean,
    val totalOweAmount: String,
    val totalOwedAmount: String,
    val paidOffLoanVisibility: Boolean,
    val dateTime: Instant,
    val selectedTab: LoanTab
)

package com.ivy.loans.loan

import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.loans.model.DisplayLoan
import com.ivy.loans.modal.LoanModalData
import kotlinx.collections.immutable.ImmutableList
import java.time.Instant

internal data class LoanScreenState(
    val baseCurrency: String,
    val completedLoans: ImmutableList<DisplayLoan>,
    val pendingLoans: ImmutableList<DisplayLoan>,
    val accounts: ImmutableList<LegacyAccount>,
    val loanModalData: LoanModalData?,
    val reorderModalVisible: Boolean,
    val totalOweAmount: String,
    val totalOwedAmount: String,
    val paidOffLoanVisibility: Boolean,
    val dateTime: Instant,
    val selectedTab: LoanTab
)

package com.ivy.loans.loandetails

import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.Loan
import com.ivy.loans.loan.data.DisplayLoanRecord
import com.ivy.legacy.ui.modal.LoanModalData
import com.ivy.legacy.ui.modal.LoanRecordModalData
import kotlinx.collections.immutable.ImmutableList
import java.time.Instant

data class LoanDetailsScreenState(
    val baseCurrency: String,
    val loan: Loan?,
    val displayLoanRecords: ImmutableList<DisplayLoanRecord>,
    val loanTotalAmount: Double,
    val amountPaid: Double,
    val loanAmountPaid: Double,
    val accounts: ImmutableList<LegacyAccount>,
    val selectedLoanAccount: LegacyAccount?,
    val createLoanTransaction: Boolean,
    val loanModalData: LoanModalData?,
    val loanRecordModalData: LoanRecordModalData?,
    val waitModalVisible: Boolean,
    val isDeleteModalVisible: Boolean,
    val dateTime: Instant
)

package com.ivy.loans.loandetails

import com.ivy.data.model.Loan
import com.ivy.loans.model.DisplayLoanRecord
import com.ivy.loans.model.LoanAccount
import com.ivy.loans.modal.LoanRecordModalData
import kotlinx.collections.immutable.ImmutableList
import java.time.Instant
import java.util.UUID

internal data class LoanDetailsScreenState(
    val baseCurrency: String,
    val loan: Loan?,
    val displayLoanRecords: ImmutableList<DisplayLoanRecord>,
    val loanTotalAmount: Double,
    val amountPaid: Double,
    val loanAmountPaid: Double,
    val accounts: ImmutableList<LoanAccount>,
    val selectedLoanAccountId: UUID?,
    val createLoanTransaction: Boolean,
    val loanModalVisible: Boolean,
    val loanModalLoan: Loan?,
    val loanModalAutoFocusKeyboard: Boolean,
    val loanModalAutoOpenAmountModal: Boolean,
    val loanRecordModalData: LoanRecordModalData?,
    val waitModalVisible: Boolean,
    val isDeleteModalVisible: Boolean,
    val dateTime: Instant
)

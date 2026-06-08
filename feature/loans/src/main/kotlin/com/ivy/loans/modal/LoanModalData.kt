package com.ivy.loans.modal

import com.ivy.data.model.Loan
import java.util.UUID

internal data class LoanModalData(
    val loan: Loan?,
    val baseCurrency: String,
    val selectedAccountId: UUID? = null,
    val autoFocusKeyboard: Boolean = true,
    val autoOpenAmountModal: Boolean = false,
    val createLoanTransaction: Boolean = false,
    val id: UUID = UUID.randomUUID()
)

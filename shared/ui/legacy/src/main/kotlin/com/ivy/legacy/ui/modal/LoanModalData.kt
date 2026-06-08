package com.ivy.legacy.ui.modal

import com.ivy.data.model.Loan
import com.ivy.data.model.legacy.LegacyAccount
import java.util.UUID

data class LoanModalData(
    val loan: Loan?,
    val baseCurrency: String,
    val selectedAccount: LegacyAccount? = null,
    val autoFocusKeyboard: Boolean = true,
    val autoOpenAmountModal: Boolean = false,
    val createLoanTransaction: Boolean = false,
    val id: UUID = UUID.randomUUID()
)

package com.ivy.legacy.ui.modal

import com.ivy.data.model.LoanRecord
import java.util.UUID

data class LoanRecordModalData(
    val loanRecord: LoanRecord?,
    val baseCurrency: String,
    val loanAccountCurrencyCode: String? = null,
    val selectedAccountId: UUID? = null,
    val createLoanRecordTransaction: Boolean = false,
    val isLoanInterest: Boolean = false,
    val id: UUID = UUID.randomUUID(),
)

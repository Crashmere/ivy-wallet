package com.ivy.loans.loan.data

import com.ivy.legacy.domain.model.Account
import com.ivy.legacy.domain.model.LoanRecord

data class DisplayLoanRecord(
    val loanRecord: LoanRecord,
    val account: Account? = null,
    val loanRecordCurrencyCode: String = "",
    val loanCurrencyCode: String = "",
    val loanRecordTransaction: Boolean = false,
)

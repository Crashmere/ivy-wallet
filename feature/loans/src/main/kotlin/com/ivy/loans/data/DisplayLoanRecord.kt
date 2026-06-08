package com.ivy.loans.data

import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.LoanRecord

internal data class DisplayLoanRecord(
    val loanRecord: LoanRecord,
    val account: LegacyAccount? = null,
    val loanRecordCurrencyCode: String = "",
    val loanCurrencyCode: String = "",
    val loanRecordTransaction: Boolean = false,
)

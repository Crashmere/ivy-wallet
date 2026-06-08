package com.ivy.loans.model

import com.ivy.data.model.LoanRecord
import java.util.UUID

internal data class DisplayLoanAccount(
    val id: UUID,
    val name: String,
    val icon: String?,
)

internal data class DisplayLoanRecord(
    val loanRecord: LoanRecord,
    val account: DisplayLoanAccount? = null,
    val loanRecordCurrencyCode: String = "",
    val loanCurrencyCode: String = "",
    val loanRecordTransaction: Boolean = false,
)

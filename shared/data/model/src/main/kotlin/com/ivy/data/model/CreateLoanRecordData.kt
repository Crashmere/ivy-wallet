package com.ivy.data.model

import com.ivy.data.model.legacy.LegacyAccount
import java.time.Instant

data class CreateLoanRecordData(
    val note: String?,
    val amount: Double,
    val dateTime: Instant,
    val interest: Boolean = false,
    val account: LegacyAccount? = null,
    val createLoanRecordTransaction: Boolean = false,
    val convertedAmount: Double? = null,
    val loanRecordType: LoanRecordType
)

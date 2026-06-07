package com.ivy.data.model

import java.time.Instant
import java.util.UUID

data class LoanRecord(
    val loanId: UUID,
    val amount: Double,
    val note: String? = null,
    val dateTime: Instant,
    val interest: Boolean = false,
    val accountId: UUID? = null,
    // This stores the converted amount for currencies which are different from the loan account currency.
    val convertedAmount: Double? = null,
    val loanRecordType: LoanRecordType,

    val isDeleted: Boolean = false,

    val id: UUID = UUID.randomUUID()
)

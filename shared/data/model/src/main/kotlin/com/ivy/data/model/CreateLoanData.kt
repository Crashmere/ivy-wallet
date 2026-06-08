package com.ivy.data.model

import java.time.LocalDateTime
import java.util.UUID

data class CreateLoanData(
    val name: String,
    val amount: Double,
    val type: LoanType,
    val color: Int,
    val icon: String?,
    val accountId: UUID? = null,
    val note: String?,
    val createLoanTransaction: Boolean = false,
    val dateTime: LocalDateTime
)

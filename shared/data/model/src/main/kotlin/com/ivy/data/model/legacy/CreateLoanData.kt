package com.ivy.data.model.legacy

import com.ivy.data.model.LoanType
import java.time.LocalDateTime

data class CreateLoanData(
    val name: String,
    val amount: Double,
    val type: LoanType,
    val color: Int,
    val icon: String?,
    val account: Account? = null,
    val note: String?,
    val createLoanTransaction: Boolean = false,
    val dateTime: LocalDateTime
)

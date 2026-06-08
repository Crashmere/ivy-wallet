package com.ivy.data.model

import com.ivy.data.model.legacy.LegacyAccount
import java.time.LocalDateTime

data class CreateLoanData(
    val name: String,
    val amount: Double,
    val type: LoanType,
    val color: Int,
    val icon: String?,
    val account: LegacyAccount? = null,
    val note: String?,
    val createLoanTransaction: Boolean = false,
    val dateTime: LocalDateTime
)

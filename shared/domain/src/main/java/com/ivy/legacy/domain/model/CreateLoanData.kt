package com.ivy.legacy.domain.model

import com.ivy.data.model.LoanType
import com.ivy.legacy.datamodel.Account
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

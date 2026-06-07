package com.ivy.data.model.legacy

import com.ivy.data.model.LoanType
import java.time.LocalDateTime
import java.util.UUID

@Suppress("DataClassDefaultValues")
data class Loan(
    val name: String,
    val amount: Double,
    val type: LoanType,
    val color: Int = 0,
    val icon: String? = null,
    val orderNum: Double = 0.0,
    val accountId: UUID? = null,
    val note: String? = null,

    val isDeleted: Boolean = false,
    val dateTime: LocalDateTime? = null,

    val id: UUID = UUID.randomUUID()
)

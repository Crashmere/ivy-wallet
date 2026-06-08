package com.ivy.transaction

import com.ivy.data.model.TransactionType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

internal data class EditTransactionDraft(
    val accountId: UUID,
    val type: TransactionType,
    val amount: BigDecimal,
    val toAccountId: UUID? = null,
    val toAmount: BigDecimal = amount,
    val title: String? = null,
    val description: String? = null,
    val dateTime: Instant? = null,
    val categoryId: UUID? = null,
    val dueDate: Instant? = null,
    val recurringRuleId: UUID? = null,
    val paidFor: Instant? = null,
    val loanId: UUID? = null,
    val loanRecordId: UUID? = null,
    val id: UUID = UUID.randomUUID(),
)

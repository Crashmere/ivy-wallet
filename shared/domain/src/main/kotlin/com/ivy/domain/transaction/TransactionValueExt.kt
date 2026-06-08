package com.ivy.domain.transaction

import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

internal fun Transaction.getValue(): BigDecimal = when (this) {
    is Expense -> value.amount.value.toBigDecimal()
    is Income -> value.amount.value.toBigDecimal()
    is Transfer -> fromValue.amount.value.toBigDecimal()
}

internal fun Transaction.getAccountId(): UUID = when (this) {
    is Expense -> account.value
    is Income -> account.value
    is Transfer -> fromAccount.value
}

internal fun Transaction.settleNow(): Transaction {
    val timeNow = Instant.now()
    return when (this) {
        is Income -> copy(
            settled = true,
            time = timeNow
        )

        is Expense -> copy(
            settled = true,
            time = timeNow
        )

        is Transfer -> copy(
            settled = true,
            time = timeNow
        )
    }
}

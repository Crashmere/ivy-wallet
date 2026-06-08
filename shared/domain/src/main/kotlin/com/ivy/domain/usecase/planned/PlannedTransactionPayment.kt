package com.ivy.domain.usecase.planned

import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import java.time.Instant

internal fun Transaction.markPlannedPaymentPaid(
    paidAt: Instant,
    paidFor: Instant?,
): Transaction {
    return when (this) {
        is Income -> copy(
            time = paidAt,
            settled = true,
            metadata = metadata.copy(paidForDateTime = paidFor)
        )

        is Expense -> copy(
            time = paidAt,
            settled = true,
            metadata = metadata.copy(paidForDateTime = paidFor)
        )

        is Transfer -> copy(
            time = paidAt,
            settled = true,
            metadata = metadata.copy(paidForDateTime = paidFor)
        )
    }
}

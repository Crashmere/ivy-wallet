package com.ivy.domain.usecase.transaction

import com.ivy.data.model.primitive.NonNegativeLong
import com.ivy.data.api.TransactionStore
import com.ivy.domain.time.nowLocalDate
import com.ivy.domain.time.toUtcInstant
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

private fun LocalDate.atEndOfDay(): LocalDateTime =
    atTime(23, 59, 59)

class CountTodayTransactionsUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(): NonNegativeLong {
        val today = nowLocalDate()
        return transactionStore.countBetween(
            startDate = today.atStartOfDay().toUtcInstant(),
            endDate = today.atEndOfDay().toUtcInstant(),
        )
    }
}

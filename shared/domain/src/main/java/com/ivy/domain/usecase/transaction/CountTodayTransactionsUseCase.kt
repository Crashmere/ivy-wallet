package com.ivy.domain.usecase.transaction

import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.base.time.atEndOfDay
import com.ivy.data.model.primitive.NonNegativeLong
import com.ivy.data.api.TransactionStore
import javax.inject.Inject

class CountTodayTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionStore,
    private val timeProvider: TimeProvider,
    private val timeConverter: TimeConverter,
) {
    suspend operator fun invoke(): NonNegativeLong {
        val today = timeProvider.localDateNow()
        return with(timeConverter) {
            transactionRepository.countBetween(
                startDate = today.atStartOfDay().toUTC(),
                endDate = today.atEndOfDay().toUTC(),
            )
        }
    }
}

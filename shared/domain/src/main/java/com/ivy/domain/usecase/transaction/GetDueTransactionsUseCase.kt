package com.ivy.domain.usecase.transaction

import com.ivy.data.model.Transaction
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.data.api.TransactionStore
import javax.inject.Inject

class GetDueTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionStore
) {
    suspend operator fun invoke(range: ClosedTimeRange): List<Transaction> {
        return transactionRepository.findAllDueToBetween(
            startDate = range.from,
            endDate = range.to
        )
    }
}

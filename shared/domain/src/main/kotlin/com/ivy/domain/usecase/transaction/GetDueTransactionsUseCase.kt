package com.ivy.domain.usecase.transaction

import com.ivy.data.model.Transaction
import com.ivy.data.model.ClosedTimeRange
import com.ivy.data.api.TransactionStore
import javax.inject.Inject

class GetDueTransactionsUseCase @Inject constructor(
    private val transactionStore: TransactionStore
) {
    suspend operator fun invoke(range: ClosedTimeRange): List<Transaction> {
        return transactionStore.findAllDueToBetween(
            startDate = range.from,
            endDate = range.to
        )
    }
}

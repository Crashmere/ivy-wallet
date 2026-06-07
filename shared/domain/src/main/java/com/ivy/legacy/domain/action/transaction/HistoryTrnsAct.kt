package com.ivy.legacy.domain.action.transaction

import com.ivy.data.model.Transaction
import com.ivy.data.repository.TransactionRepository
import com.ivy.legacy.frp.action.FPAction
import com.ivy.data.model.legacy.ClosedTimeRange
import javax.inject.Inject

class HistoryTrnsAct @Inject constructor(
    private val transactionRepository: TransactionRepository
) : FPAction<ClosedTimeRange, List<Transaction>>() {

    override suspend fun ClosedTimeRange.compose(): suspend () -> List<Transaction> = suspend {
        io {
            transactionRepository.findAllBetween(
                startDate = from,
                endDate = to
            )
        }
    }
}

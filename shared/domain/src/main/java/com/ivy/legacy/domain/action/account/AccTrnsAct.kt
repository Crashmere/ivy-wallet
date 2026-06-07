package com.ivy.legacy.domain.action.account

import com.ivy.data.model.AccountId
import com.ivy.data.model.Transaction
import com.ivy.data.repository.TransactionRepository
import com.ivy.base.frp.action.FPAction
import com.ivy.data.model.legacy.ClosedTimeRange
import java.util.UUID
import javax.inject.Inject

class AccTrnsAct @Inject constructor(
    private val transactionRepository: TransactionRepository
) : FPAction<AccTrnsAct.Input, List<Transaction>>() {
    override suspend fun Input.compose(): suspend () -> List<Transaction> = suspend {
        io {
            transactionRepository.findAllByAccountAndBetween(
                accountId = AccountId(accountId),
                startDate = range.from,
                endDate = range.to
            ) + transactionRepository.findAllToAccountAndBetween(
                toAccountId = AccountId(accountId),
                startDate = range.from,
                endDate = range.to
            )
        }
    }

    class Input(
        val accountId: UUID,
        val range: ClosedTimeRange
    )
}

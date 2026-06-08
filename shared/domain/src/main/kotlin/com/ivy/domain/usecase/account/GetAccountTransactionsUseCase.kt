package com.ivy.domain.usecase.account

import com.ivy.data.model.AccountId
import com.ivy.data.model.Transaction
import com.ivy.data.model.ClosedTimeRange
import com.ivy.data.api.TransactionStore
import javax.inject.Inject

class GetAccountTransactionsUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore
) {
    suspend operator fun invoke(
        accountId: AccountId,
        range: ClosedTimeRange
    ): List<Transaction> {
        return transactionStore.findAllByAccountAndBetween(
            accountId = accountId,
            startDate = range.from,
            endDate = range.to
        ) + transactionStore.findAllToAccountAndBetween(
            toAccountId = accountId,
            startDate = range.from,
            endDate = range.to
        )
    }
}

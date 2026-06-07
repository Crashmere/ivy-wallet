package com.ivy.domain.usecase.account

import com.ivy.data.model.AccountId
import com.ivy.data.model.Transaction
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.data.repository.TransactionRepository
import javax.inject.Inject

class GetAccountTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        accountId: AccountId,
        range: ClosedTimeRange
    ): List<Transaction> {
        return transactionRepository.findAllByAccountAndBetween(
            accountId = accountId,
            startDate = range.from,
            endDate = range.to
        ) + transactionRepository.findAllToAccountAndBetween(
            toAccountId = accountId,
            startDate = range.from,
            endDate = range.to
        )
    }
}

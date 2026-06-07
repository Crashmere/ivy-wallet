package com.ivy.domain.usecase.account

import com.ivy.base.time.TimeProvider
import com.ivy.data.model.AccountId
import com.ivy.data.model.Transaction
import com.ivy.data.model.legacy.FromToTimeRange
import com.ivy.data.repository.TransactionRepository
import com.ivy.legacy.domain.time.filterOverdue
import javax.inject.Inject

class GetAccountOverdueTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(
        accountId: AccountId,
        range: FromToTimeRange
    ): List<Transaction> {
        return transactionRepository.findAllDueToBetweenByAccount(
            accountId = accountId,
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        ).filterOverdue(timeProvider)
    }
}

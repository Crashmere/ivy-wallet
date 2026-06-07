package com.ivy.domain.usecase.account

import com.ivy.data.model.AccountId
import com.ivy.data.model.Transaction
import com.ivy.data.model.legacy.FromToTimeRange
import com.ivy.data.api.TransactionStore
import com.ivy.domain.time.filterOverdue
import com.ivy.domain.time.nowUtc
import javax.inject.Inject

class GetAccountOverdueTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionStore,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        range: FromToTimeRange
    ): List<Transaction> {
        return transactionRepository.findAllDueToBetweenByAccount(
            accountId = accountId,
            startDate = range.from(),
            endDate = range.overdueTo(nowUtc())
        ).filterOverdue()
    }
}

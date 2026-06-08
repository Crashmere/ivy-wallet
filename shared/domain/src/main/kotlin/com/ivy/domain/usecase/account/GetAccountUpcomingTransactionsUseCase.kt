package com.ivy.domain.usecase.account

import com.ivy.data.model.AccountId
import com.ivy.data.model.Transaction
import com.ivy.data.model.FromToTimeRange
import com.ivy.data.api.TransactionStore
import com.ivy.domain.time.filterUpcoming
import com.ivy.domain.time.nowUtc
import javax.inject.Inject

internal class GetAccountUpcomingTransactionsUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        range: FromToTimeRange
    ): List<Transaction> {
        return transactionStore.findAllDueToBetweenByAccount(
            accountId = accountId,
            startDate = range.upcomingFrom(nowUtc()),
            endDate = range.to()
        ).filterUpcoming()
    }
}

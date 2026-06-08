package com.ivy.domain.usecase.transaction

import com.ivy.data.api.TransactionStore
import com.ivy.data.model.FromToTimeRange
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.domain.mapper.legacy.toLegacyTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetLegacyTransactionsForAccountsUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        range: FromToTimeRange,
        accountIdFilterSet: Set<UUID>
    ): List<LegacyTransaction> {
        return withContext(Dispatchers.IO) {
            transactionStore.findAllBetween(range.from(), range.to())
                .map { it.toLegacyTransaction() }
                .filter {
                    accountIdFilterSet.contains(it.accountId) ||
                            accountIdFilterSet.contains(it.toAccountId)
                }
        }
    }
}

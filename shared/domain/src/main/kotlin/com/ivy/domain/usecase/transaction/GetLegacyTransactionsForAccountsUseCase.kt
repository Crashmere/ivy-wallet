package com.ivy.domain.usecase.transaction

import com.ivy.data.api.TransactionStore
import com.ivy.data.model.legacy.FromToTimeRange
import com.ivy.data.model.legacy.Transaction
import com.ivy.domain.mapper.legacy.toLegacy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetLegacyTransactionsForAccountsUseCase @Inject constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        range: FromToTimeRange,
        accountIdFilterSet: Set<UUID>
    ): List<Transaction> {
        return withContext(Dispatchers.IO) {
            transactionStore.findAllBetween(range.from(), range.to())
                .map { it.toLegacy() }
                .filter {
                    accountIdFilterSet.contains(it.accountId) ||
                            accountIdFilterSet.contains(it.toAccountId)
                }
        }
    }
}

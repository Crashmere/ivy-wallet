package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.Transaction
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.legacy.FromToTimeRange
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.domain.mapper.legacy.toLegacy
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetLegacyTransactionsForAccountsUseCase @Inject constructor(
    private val transactionStore: TransactionStore,
    private val transactionMapper: TransactionMapper,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(
        range: FromToTimeRange,
        accountIdFilterSet: Set<UUID>
    ): List<Transaction> {
        return withContext(dispatchers.io) {
            transactionStore.findAllBetween(range.from(), range.to())
                .map { it.toLegacy(transactionMapper) }
                .filter {
                    accountIdFilterSet.contains(it.accountId) ||
                            accountIdFilterSet.contains(it.toAccountId)
                }
        }
    }
}

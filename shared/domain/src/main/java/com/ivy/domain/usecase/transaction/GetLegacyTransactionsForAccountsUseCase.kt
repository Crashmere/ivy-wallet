package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.Transaction
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.TransactionDao
import com.ivy.data.model.legacy.FromToTimeRange
import com.ivy.domain.mapper.legacy.toLegacyDomain
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetLegacyTransactionsForAccountsUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(
        range: FromToTimeRange,
        accountIdFilterSet: Set<UUID>
    ): List<Transaction> {
        return withContext(dispatchers.io) {
            transactionDao.findAllBetween(range.from(), range.to())
                .map { it.toLegacyDomain() }
                .filter {
                    accountIdFilterSet.contains(it.accountId) ||
                            accountIdFilterSet.contains(it.toAccountId)
                }
        }
    }
}

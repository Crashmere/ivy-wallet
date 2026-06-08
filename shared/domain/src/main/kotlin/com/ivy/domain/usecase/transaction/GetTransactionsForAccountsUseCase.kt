package com.ivy.domain.usecase.transaction

import com.ivy.data.api.TransactionStore
import com.ivy.data.model.FromToTimeRange
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.getFromAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetTransactionsForAccountsUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        range: FromToTimeRange,
        accountIdFilterSet: Set<UUID>
    ): List<Transaction> {
        return withContext(Dispatchers.IO) {
            transactionStore.findAllBetween(range.from(), range.to())
                .filter { transaction ->
                    accountIdFilterSet.contains(transaction.getFromAccount().value) ||
                            transaction is Transfer &&
                            accountIdFilterSet.contains(transaction.toAccount.value)
                }
        }
    }
}

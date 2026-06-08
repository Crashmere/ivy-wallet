package com.ivy.domain.usecase.transaction

import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetTransactionsByIdsUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(ids: List<UUID>): List<Transaction> {
        return withContext(Dispatchers.IO) {
            if (ids.isEmpty()) {
                return@withContext emptyList()
            }

            val orderById = ids.withIndex().associate { it.value to it.index }
            transactionStore.findByIds(ids.map(::TransactionId))
                .sortedBy { orderById[it.id.value] ?: Int.MAX_VALUE }
        }
    }
}

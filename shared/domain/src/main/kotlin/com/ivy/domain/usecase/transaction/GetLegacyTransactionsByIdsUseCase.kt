package com.ivy.domain.usecase.transaction

import com.ivy.data.api.TransactionStore
import com.ivy.data.model.TransactionId
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.domain.mapper.legacy.toLegacyTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetLegacyTransactionsByIdsUseCase @Inject constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(ids: List<UUID>): List<LegacyTransaction> {
        return withContext(Dispatchers.IO) {
            if (ids.isEmpty()) {
                return@withContext emptyList()
            }

            val orderById = ids.withIndex().associate { it.value to it.index }
            transactionStore.findByIds(ids.map(::TransactionId))
                .map { it.toLegacyTransaction() }
                .sortedBy { orderById[it.id] ?: Int.MAX_VALUE }
        }
    }
}

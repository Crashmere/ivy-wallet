package com.ivy.domain.usecase.transaction

import com.ivy.data.model.legacy.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.api.TransactionStore
import com.ivy.domain.mapper.legacy.toLegacy
import java.util.UUID
import javax.inject.Inject

class GetLegacyTransactionUseCase @Inject constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(id: UUID): Transaction? {
        return transactionStore.findById(TransactionId(id))
            ?.toLegacy()
    }
}

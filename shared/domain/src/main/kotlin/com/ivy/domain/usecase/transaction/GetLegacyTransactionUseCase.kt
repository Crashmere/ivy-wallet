package com.ivy.domain.usecase.transaction

import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.TransactionId
import com.ivy.data.api.TransactionStore
import com.ivy.domain.mapper.legacy.toLegacyTransaction
import java.util.UUID
import javax.inject.Inject

class GetLegacyTransactionUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(id: UUID): LegacyTransaction? {
        return transactionStore.findById(TransactionId(id))
            ?.toLegacyTransaction()
    }
}

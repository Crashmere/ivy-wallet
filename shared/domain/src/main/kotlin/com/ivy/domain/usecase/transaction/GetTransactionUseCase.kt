package com.ivy.domain.usecase.transaction

import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import java.util.UUID
import javax.inject.Inject

class GetTransactionUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(id: UUID): Transaction? {
        return transactionStore.findById(TransactionId(id))
    }
}

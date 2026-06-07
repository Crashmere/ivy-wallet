package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.api.TransactionStore
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.domain.mapper.legacy.toLegacy
import java.util.UUID
import javax.inject.Inject

class GetLegacyTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionStore,
    private val mapper: TransactionMapper
) {
    suspend operator fun invoke(id: UUID): Transaction? {
        return transactionRepository.findById(TransactionId(id))
            ?.toLegacy(mapper)
    }
}

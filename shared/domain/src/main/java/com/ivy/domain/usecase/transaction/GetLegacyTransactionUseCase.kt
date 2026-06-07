package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.repository.TransactionRepository
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.legacy.domain.mapper.toLegacy
import java.util.UUID
import javax.inject.Inject

class GetLegacyTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val mapper: TransactionMapper
) {
    suspend operator fun invoke(id: UUID): Transaction? {
        return transactionRepository.findById(TransactionId(id))
            ?.toLegacy(mapper)
    }
}

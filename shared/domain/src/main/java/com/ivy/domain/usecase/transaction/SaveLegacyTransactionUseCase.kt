package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.LegacyTransaction
import com.ivy.data.repository.TransactionRepository
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.domain.mapper.legacy.toDomain
import javax.inject.Inject

class SaveLegacyTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val transactionMapper: TransactionMapper
) {
    suspend operator fun invoke(transaction: LegacyTransaction) {
        transaction.toDomain(transactionMapper)?.let {
            transactionRepository.save(it)
        }
    }
}

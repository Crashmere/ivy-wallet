package com.ivy.domain.usecase.transaction

import com.ivy.data.model.TransactionId
import com.ivy.data.api.TransactionStore
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionStore
) {
    suspend operator fun invoke(transactionId: TransactionId) {
        transactionRepository.deleteById(transactionId)
    }
}

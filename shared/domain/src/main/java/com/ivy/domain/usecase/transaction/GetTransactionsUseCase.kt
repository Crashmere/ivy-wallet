package com.ivy.domain.usecase.transaction

import com.ivy.data.model.Transaction
import com.ivy.data.api.TransactionStore
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionStore
) {
    suspend operator fun invoke(): List<Transaction> {
        return transactionRepository.findAll()
    }
}

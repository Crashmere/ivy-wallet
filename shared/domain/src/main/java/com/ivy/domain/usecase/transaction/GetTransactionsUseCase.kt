package com.ivy.domain.usecase.transaction

import com.ivy.data.model.Transaction
import com.ivy.data.repository.TransactionRepository
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(): List<Transaction> {
        return transactionRepository.findAll()
    }
}

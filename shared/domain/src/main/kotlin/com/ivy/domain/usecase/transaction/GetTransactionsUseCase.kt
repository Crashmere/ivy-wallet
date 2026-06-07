package com.ivy.domain.usecase.transaction

import com.ivy.data.model.Transaction
import com.ivy.data.api.TransactionStore
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionStore: TransactionStore
) {
    suspend operator fun invoke(): List<Transaction> {
        return transactionStore.findAll()
    }
}

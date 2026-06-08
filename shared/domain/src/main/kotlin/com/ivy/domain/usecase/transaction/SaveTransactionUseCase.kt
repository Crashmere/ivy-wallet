package com.ivy.domain.usecase.transaction

import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Transaction
import javax.inject.Inject

class SaveTransactionUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(transaction: Transaction) {
        transactionStore.save(transaction)
    }
}

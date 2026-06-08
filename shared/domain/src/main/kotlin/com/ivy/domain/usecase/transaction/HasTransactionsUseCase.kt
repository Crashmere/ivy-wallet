package com.ivy.domain.usecase.transaction

import com.ivy.data.api.TransactionStore
import javax.inject.Inject

class HasTransactionsUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(): Boolean {
        return transactionStore.hasAny()
    }
}

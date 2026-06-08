package com.ivy.domain.usecase.transaction

import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.api.AccountStore
import com.ivy.data.api.TransactionStore
import com.ivy.domain.mapper.legacy.toTransaction
import javax.inject.Inject

class SaveLegacyTransactionUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
    private val accountStore: AccountStore,
) {
    suspend operator fun invoke(transaction: LegacyTransaction) {
        transaction.toTransaction(accountStore)?.let {
            transactionStore.save(it)
        }
    }
}

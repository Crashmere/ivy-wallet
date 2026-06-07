package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.LegacyTransaction
import com.ivy.data.api.AccountStore
import com.ivy.data.api.TransactionStore
import com.ivy.domain.mapper.legacy.toDomain
import javax.inject.Inject

class SaveLegacyTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionStore,
    private val accountStore: AccountStore,
) {
    suspend operator fun invoke(transaction: LegacyTransaction) {
        transaction.toDomain(accountStore)?.let {
            transactionRepository.save(it)
        }
    }
}

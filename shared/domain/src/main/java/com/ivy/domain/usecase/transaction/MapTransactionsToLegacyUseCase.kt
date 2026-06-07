package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.LegacyTransaction
import com.ivy.data.model.Transaction
import com.ivy.domain.mapper.legacy.toLegacy
import javax.inject.Inject

class MapTransactionsToLegacyUseCase @Inject constructor() {
    operator fun invoke(transactions: List<Transaction>): List<LegacyTransaction> {
        return transactions.map { it.toLegacy() }
    }
}

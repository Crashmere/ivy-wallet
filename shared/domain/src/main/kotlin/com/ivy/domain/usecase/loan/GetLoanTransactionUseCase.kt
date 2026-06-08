package com.ivy.domain.usecase.loan

import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.api.TransactionStore
import com.ivy.domain.mapper.legacy.toLegacyTransaction
import java.util.UUID
import javax.inject.Inject

class GetLoanTransactionUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(loanId: UUID): LegacyTransaction? {
        return transactionStore.findLoanTransaction(loanId)
            ?.toLegacyTransaction()
    }
}

package com.ivy.domain.usecase.loan

import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Transaction
import java.util.UUID
import javax.inject.Inject

class GetLoanTransactionUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(loanId: UUID): Transaction? {
        return transactionStore.findLoanTransaction(loanId)
    }
}

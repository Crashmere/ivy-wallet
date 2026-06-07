package com.ivy.domain.usecase.loan

import com.ivy.data.api.TransactionStore
import java.util.UUID
import javax.inject.Inject

class HasLoanRecordTransactionUseCase @Inject constructor(
    private val transactionStore: TransactionStore
) {
    suspend operator fun invoke(loanRecordId: UUID): Boolean {
        return transactionStore.findLoanRecordTransaction(loanRecordId) != null
    }
}

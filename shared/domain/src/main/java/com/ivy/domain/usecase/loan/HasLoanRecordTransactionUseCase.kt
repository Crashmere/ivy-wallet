package com.ivy.domain.usecase.loan

import com.ivy.data.api.TransactionStore
import java.util.UUID
import javax.inject.Inject

class HasLoanRecordTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionStore
) {
    suspend operator fun invoke(loanRecordId: UUID): Boolean {
        return transactionRepository.findLoanRecordTransaction(loanRecordId) != null
    }
}

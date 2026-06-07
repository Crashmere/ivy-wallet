package com.ivy.domain.usecase.loan

import com.ivy.data.repository.TransactionRepository
import java.util.UUID
import javax.inject.Inject

class HasLoanRecordTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(loanRecordId: UUID): Boolean {
        return transactionRepository.findLoanRecordTransaction(loanRecordId) != null
    }
}

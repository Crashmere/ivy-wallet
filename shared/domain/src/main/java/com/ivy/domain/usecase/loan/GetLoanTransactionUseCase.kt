package com.ivy.domain.usecase.loan

import com.ivy.base.model.legacy.LegacyTransaction
import com.ivy.data.repository.TransactionRepository
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.legacy.domain.mapper.toLegacy
import java.util.UUID
import javax.inject.Inject

class GetLoanTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val transactionMapper: TransactionMapper
) {
    suspend operator fun invoke(loanId: UUID): LegacyTransaction? {
        return transactionRepository.findLoanTransaction(loanId)
            ?.toLegacy(transactionMapper)
    }
}

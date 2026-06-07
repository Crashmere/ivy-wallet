package com.ivy.domain.usecase.loan

import com.ivy.base.model.legacy.LegacyTransaction
import com.ivy.data.api.TransactionStore
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.domain.mapper.legacy.toLegacy
import java.util.UUID
import javax.inject.Inject

class GetLoanTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionStore,
    private val transactionMapper: TransactionMapper
) {
    suspend operator fun invoke(loanId: UUID): LegacyTransaction? {
        return transactionRepository.findLoanTransaction(loanId)
            ?.toLegacy(transactionMapper)
    }
}

package com.ivy.domain.usecase.loan

import com.ivy.data.model.legacy.LegacyTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateAssociatedLoanDataUseCase @Inject constructor(
    private val loanTransactionSyncUseCase: LoanTransactionSyncUseCase,
    private val loanRecordTransactionSyncUseCase: LoanRecordTransactionSyncUseCase
) {
    suspend operator fun invoke(
        transaction: LegacyTransaction?,
        onBackgroundProcessingStart: suspend () -> Unit = {},
        onBackgroundProcessingEnd: suspend () -> Unit = {},
        accountsChanged: Boolean = true
    ) {
        withContext(Dispatchers.Default) {
            if (transaction == null) {
                return@withContext
            }

            if (transaction.loanId != null && transaction.loanRecordId == null) {
                loanTransactionSyncUseCase.updateAssociatedLoan(
                    transaction = transaction,
                    onBackgroundProcessingStart = onBackgroundProcessingStart,
                    onBackgroundProcessingEnd = onBackgroundProcessingEnd,
                    accountsChanged = accountsChanged
                )
            } else if (transaction.loanId != null && transaction.loanRecordId != null) {
                loanRecordTransactionSyncUseCase.updateAssociatedLoanRecord(
                    transaction = transaction,
                    onBackgroundProcessingStart = onBackgroundProcessingStart,
                    onBackgroundProcessingEnd = onBackgroundProcessingEnd
                )
            }
        }
    }
}

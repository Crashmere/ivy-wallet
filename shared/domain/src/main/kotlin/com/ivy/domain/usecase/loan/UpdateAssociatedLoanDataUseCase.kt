package com.ivy.domain.usecase.loan

import com.ivy.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateAssociatedLoanDataUseCase @Inject internal constructor(
    private val loanTransactionSyncUseCase: LoanTransactionSyncUseCase,
    private val loanRecordTransactionSyncUseCase: LoanRecordTransactionSyncUseCase
) {
    suspend operator fun invoke(
        transaction: Transaction?,
        onBackgroundProcessingStart: suspend () -> Unit = {},
        onBackgroundProcessingEnd: suspend () -> Unit = {},
        accountsChanged: Boolean = true
    ) {
        withContext(Dispatchers.Default) {
            if (transaction == null) {
                return@withContext
            }

            val loanId = transaction.metadata.loanId
            val loanRecordId = transaction.metadata.loanRecordId

            if (loanId != null && loanRecordId == null) {
                loanTransactionSyncUseCase.updateAssociatedLoan(
                    transaction = transaction,
                    onBackgroundProcessingStart = onBackgroundProcessingStart,
                    onBackgroundProcessingEnd = onBackgroundProcessingEnd,
                    accountsChanged = accountsChanged
                )
            } else if (loanId != null && loanRecordId != null) {
                loanRecordTransactionSyncUseCase.updateAssociatedLoanRecord(
                    transaction = transaction,
                    onBackgroundProcessingStart = onBackgroundProcessingStart,
                    onBackgroundProcessingEnd = onBackgroundProcessingEnd
                )
            }
        }
    }
}

package com.ivy.domain.usecase.loan

import com.ivy.data.model.Loan
import com.ivy.data.model.LoanRecord
import com.ivy.data.model.CreateLoanRecordData
import com.ivy.data.model.Transaction
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getFromValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class LoanRecordTransactionSyncUseCase @Inject internal constructor(
    private val ltCore: LoanTransactionSyncCore
) {
    suspend fun editAssociatedLoanRecordTransaction(
        loan: Loan,
        loanRecord: LoanRecord,
        createLoanRecordTransaction: Boolean,
    ) {
        withContext(Dispatchers.Default) {
            val transaction = ltCore.fetchLoanRecordTransaction(loanRecord.id)
            ltCore.updateAssociatedTransaction(
                createTransaction = createLoanRecordTransaction,
                loanRecordId = loanRecord.id,
                loanId = loan.id,
                amount = loanRecord.amount,
                loanType = loan.type,
                selectedAccountId = loanRecord.accountId,
                title = loanRecord.note,
                time = loanRecord.dateTime,
                isLoanRecord = true,
                transaction = transaction,
                loanRecordType = loanRecord.loanRecordType
            )
        }
    }

    suspend fun createAssociatedLoanRecordTransaction(
        loan: Loan,
        loanRecordId: UUID,
        data: CreateLoanRecordData,
    ) {
        withContext(Dispatchers.Default) {
            ltCore.updateAssociatedTransaction(
                createTransaction = data.createLoanRecordTransaction,
                loanType = loan.type,
                amount = data.amount,
                title = data.note,
                time = data.dateTime,
                loanRecordId = loanRecordId,
                loanId = loan.id,
                selectedAccountId = data.accountId,
                isLoanRecord = true,
                loanRecordType = data.loanRecordType
            )
        }
    }

    suspend fun deleteAssociatedLoanRecordTransaction(loanRecordId: UUID) {
        ltCore.deleteAssociatedTransactions(loanRecordId = loanRecordId)
    }

    suspend fun updateAssociatedLoanRecord(
        transaction: Transaction?,
        onBackgroundProcessingStart: suspend () -> Unit = {},
        onBackgroundProcessingEnd: suspend () -> Unit = {},
    ) {
        val loanId = transaction?.metadata?.loanId ?: return
        val loanRecordId = transaction.metadata.loanRecordId ?: return
        withContext(Dispatchers.Default) {
            onBackgroundProcessingStart()

            val loanRecord =
                ltCore.fetchLoanRecord(loanRecordId) ?: return@withContext
            val loan = ltCore.fetchLoan(loanId) ?: return@withContext

            val convertedAmount = ltCore.computeConvertedAmount(
                oldLoanRecordAccountId = loanRecord.accountId,
                oldLoanRecordConvertedAmount = loanRecord.convertedAmount,
                oldLoanRecordAmount = loanRecord.amount,
                newLoanRecordAccountId = transaction.getFromAccount().value,
                newLoanRecordAmount = transaction.getFromValue().amount.value,
                loanAccountId = loan.accountId,
                accounts = ltCore.fetchAccounts()
            )

            val modifiedLoanRecord = loanRecord.copy(
                amount = transaction.getFromValue().amount.value,
                note = transaction.title?.value,
                dateTime = transaction.time,
                accountId = transaction.getFromAccount().value,
                convertedAmount = convertedAmount
            )
            ltCore.saveLoanRecords(modifiedLoanRecord)
        }
        onBackgroundProcessingEnd()
    }

    suspend fun calculateConvertedAmount(
        loanAccountId: UUID?,
        newLoanRecord: LoanRecord,
        oldLoanRecord: LoanRecord,
        reCalculateLoanAmount: Boolean = false,
    ): Double? {
        return ltCore.computeConvertedAmount(
            oldLoanRecordAccountId = oldLoanRecord.accountId,
            oldLoanRecordConvertedAmount = oldLoanRecord.convertedAmount,
            oldLoanRecordAmount = oldLoanRecord.amount,
            newLoanRecordAccountId = newLoanRecord.accountId,
            newLoanRecordAmount = newLoanRecord.amount,
            loanAccountId = loanAccountId,
            accounts = ltCore.fetchAccounts(),
            reCalculateLoanAmount = reCalculateLoanAmount
        )
    }

    suspend fun calculateConvertedAmount(
        data: CreateLoanRecordData,
        loanAccountId: UUID?,
    ): Double? {
        return ltCore.computeConvertedAmount(
            oldLoanRecordAccountId = null,
            oldLoanRecordConvertedAmount = null,
            oldLoanRecordAmount = 0.0,
            newLoanRecordAccountId = data.accountId,
            newLoanRecordAmount = data.amount,
            loanAccountId = loanAccountId,
            accounts = ltCore.fetchAccounts(),
        )
    }
}

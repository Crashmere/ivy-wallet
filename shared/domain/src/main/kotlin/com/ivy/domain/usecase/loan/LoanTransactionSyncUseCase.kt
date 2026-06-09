package com.ivy.domain.usecase.loan

import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getFromValue
import com.ivy.data.model.LoanRecordType
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionType
import com.ivy.data.model.getTransactionType
import com.ivy.data.model.LoanType
import com.ivy.data.model.Account
import com.ivy.data.model.Loan
import com.ivy.data.model.LoanRecord
import com.ivy.data.model.CreateLoanData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class LoanTransactionSyncUseCase @Inject internal constructor(
    private val ltCore: LoanTransactionSyncCore
) {

    suspend fun createAssociatedLoanTransaction(data: CreateLoanData, loanId: UUID) {
        withContext(Dispatchers.Default) {
            ltCore.updateAssociatedTransaction(
                createTransaction = data.createLoanTransaction,
                loanId = loanId,
                amount = data.amount,
                loanType = data.type,
                selectedAccountId = data.accountId,
                title = data.name,
                isLoanRecord = false,
                loanRecordType = LoanRecordType.DECREASE
            )
        }
    }

    suspend fun editAssociatedLoanTransaction(
        loan: Loan,
        createLoanTransaction: Boolean = false,
        transaction: Transaction?
    ) {
        withContext(Dispatchers.Default) {
            ltCore.updateAssociatedTransaction(
                createTransaction = createLoanTransaction,
                loanId = loan.id,
                amount = loan.amount,
                loanType = loan.type,
                selectedAccountId = loan.accountId,
                title = loan.name,
                isLoanRecord = false,
                transaction = transaction,
                time = transaction?.time,
                loanRecordType = LoanRecordType.DECREASE
            )
        }
    }

    suspend fun deleteAssociatedLoanTransactions(loanId: UUID) {
        ltCore.deleteAssociatedTransactions(loanId = loanId)
    }

    suspend fun recalculateLoanRecords(
        oldLoanAccountId: UUID?,
        newLoanAccountId: UUID?,
        loanId: UUID
    ) {
        val accounts = ltCore.fetchAccounts()
        withContext(Dispatchers.Default) {
            if (oldLoanAccountId == newLoanAccountId || oldLoanAccountId.fetchAssociatedCurrencyCode(
                    accounts
                ) == newLoanAccountId.fetchAssociatedCurrencyCode(accounts)
            ) {
                return@withContext
            }

            val newLoanRecords = calculateLoanRecords(
                loanId = loanId,
                newAccountId = newLoanAccountId,
            )

            ltCore.saveLoanRecords(newLoanRecords)
        }
    }

    suspend fun updateAssociatedLoan(
        transaction: Transaction?,
        onBackgroundProcessingStart: suspend () -> Unit = {},
        onBackgroundProcessingEnd: suspend () -> Unit = {},
        accountsChanged: Boolean = true
    ) {
        withContext(Dispatchers.Default) {
            val loanId = transaction?.metadata?.loanId ?: return@withContext

            onBackgroundProcessingStart()

            val loan = ltCore.fetchLoan(loanId) ?: return@withContext

            if (accountsChanged) {
                val newLoanRecords: List<LoanRecord> = calculateLoanRecords(
                    loanId = loanId,
                    newAccountId = transaction.getFromAccount().value
                )
                ltCore.saveLoanRecords(newLoanRecords)
            }

            val title = transaction.title?.value
            val modifiedLoan = loan.copy(
                amount = transaction.getFromValue().amount.value,
                name = if (title.isNullOrEmpty()) loan.name else title,
                type = if (transaction.getTransactionType() == TransactionType.INCOME) {
                    LoanType.BORROW
                } else {
                    LoanType.LEND
                },
                accountId = transaction.getFromAccount().value
            )

            ltCore.saveLoan(modifiedLoan)
        }
        onBackgroundProcessingEnd()
    }

    private suspend fun calculateLoanRecords(
        newAccountId: UUID?,
        loanId: UUID
    ): List<LoanRecord> {
        return withContext(Dispatchers.IO) {
            val loanRecords =
                ltCore.fetchAllLoanRecords(loanId = loanId)
                    .map { loanRecord ->
                        async {
                            val convertedAmount: Double? =
                                ltCore.computeConvertedAmount(
                                    originalLoanRecordAccountId = loanRecord.accountId,
                                    originalLoanRecordConvertedAmount = loanRecord.convertedAmount,
                                    originalLoanRecordAmount = loanRecord.amount,
                                    newLoanRecordAccountId = loanRecord.accountId,
                                    newLoanRecordAmount = loanRecord.amount,
                                    loanAccountId = newAccountId,
                                    accounts = ltCore.fetchAccounts(),
                                )
                            loanRecord.copy(convertedAmount = convertedAmount)
                        }
                    }.awaitAll()
            loanRecords
        }
    }

    private suspend fun UUID?.fetchAssociatedCurrencyCode(accountsList: List<Account>): String {
        return ltCore.findAccount(accountsList, this)?.asset?.code ?: ltCore.baseCurrency()
    }
}

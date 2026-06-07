package com.ivy.domain.usecase.loan

import com.ivy.data.model.legacy.Transaction
import com.ivy.data.model.LoanRecordType
import com.ivy.data.model.TransactionType
import com.ivy.data.model.LoanType
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.legacy.Loan
import com.ivy.data.model.legacy.LoanRecord
import com.ivy.data.model.legacy.CreateLoanData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class LoanTransactionSyncUseCase @Inject constructor(
    private val ltCore: LoanTransactionSyncCore
) {

    suspend fun createAssociatedLoanTransaction(data: CreateLoanData, loanId: UUID) {
        withContext(Dispatchers.Default) {
            ltCore.updateAssociatedTransaction(
                createTransaction = data.createLoanTransaction,
                loanId = loanId,
                amount = data.amount,
                loanType = data.type,
                selectedAccountId = data.account?.id,
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
                time = transaction?.dateTime,
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
            transaction?.loanId ?: return@withContext

            onBackgroundProcessingStart()

            val loan = ltCore.fetchLoan(transaction.loanId!!) ?: return@withContext

            if (accountsChanged) {
                val newLoanRecords: List<LoanRecord> = calculateLoanRecords(
                    loanId = transaction.loanId!!,
                    newAccountId = transaction.accountId
                )
                ltCore.saveLoanRecords(newLoanRecords)
            }

            val modifiedLoan = loan.copy(
                amount = transaction.amount.toDouble(),
                name = if (transaction.title.isNullOrEmpty()) loan.name else transaction.title!!,
                type = if (transaction.type == TransactionType.INCOME) LoanType.BORROW else LoanType.LEND,
                accountId = transaction.accountId
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
                                    oldLoanRecordAccountId = loanRecord.accountId,
                                    oldLonRecordConvertedAmount = loanRecord.convertedAmount,
                                    oldLoanRecordAmount = loanRecord.amount,
                                    newLoanRecordAccountID = loanRecord.accountId,
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
        return ltCore.findAccount(accountsList, this)?.currency ?: ltCore.baseCurrency()
    }
}

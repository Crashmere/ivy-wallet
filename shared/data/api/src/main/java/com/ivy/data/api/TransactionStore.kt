package com.ivy.data.api

import com.ivy.base.model.TransactionType
import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.model.Transfer
import com.ivy.data.model.primitive.NonNegativeLong
import java.time.Instant
import java.util.UUID

interface TransactionStore {
    suspend fun findAll(): List<Transaction>

    suspend fun findAllIncomeByAccount(accountId: AccountId): List<Income>

    suspend fun findAllExpenseByAccount(accountId: AccountId): List<Expense>

    suspend fun findAllTransferByAccount(accountId: AccountId): List<Transfer>

    suspend fun findAllTransfersToAccount(toAccountId: AccountId): List<Transfer>

    suspend fun findAllBetween(
        startDate: Instant,
        endDate: Instant
    ): List<Transaction>

    suspend fun countBetween(
        startDate: Instant,
        endDate: Instant
    ): NonNegativeLong

    suspend fun findAllByAccountAndBetween(
        accountId: AccountId,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction>

    suspend fun findAllToAccountAndBetween(
        toAccountId: AccountId,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction>

    suspend fun findAllDueToBetween(
        startDate: Instant,
        endDate: Instant
    ): List<Transaction>

    suspend fun findAllDueToBetweenByCategory(
        startDate: Instant,
        endDate: Instant,
        categoryId: CategoryId
    ): List<Transaction>

    suspend fun findAllDueToBetweenByCategoryUnspecified(
        startDate: Instant,
        endDate: Instant
    ): List<Transaction>

    suspend fun findAllDueToBetweenByAccount(
        startDate: Instant,
        endDate: Instant,
        accountId: AccountId
    ): List<Transaction>

    suspend fun findAllByCategoryAndTypeAndBetween(
        categoryId: UUID,
        type: TransactionType,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction>

    suspend fun findAllUnspecifiedAndTypeAndBetween(
        type: TransactionType,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction>

    suspend fun findAllUnspecifiedAndBetween(
        startDate: Instant,
        endDate: Instant
    ): List<Transaction>

    suspend fun findAllByCategoryAndBetween(
        categoryId: UUID,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction>

    suspend fun findAllByRecurringRuleId(recurringRuleId: UUID): List<Transaction>

    suspend fun findById(id: TransactionId): Transaction?

    suspend fun findByIds(ids: List<TransactionId>): List<Transaction>

    suspend fun save(value: Transaction)

    suspend fun saveMany(value: List<Transaction>)

    suspend fun deleteById(id: TransactionId)

    suspend fun deleteAllByAccountId(accountId: AccountId)

    suspend fun deletedByRecurringRuleIdAndNoDateTime(recurringRuleId: UUID)

    suspend fun deleteAll()

    suspend fun countHappenedTransactions(): NonNegativeLong

    suspend fun findLoanTransaction(loanId: UUID): Transaction?

    suspend fun findLoanRecordTransaction(loanRecordId: UUID): Transaction?

    suspend fun findAllByLoanId(loanId: UUID): List<Transaction>
}

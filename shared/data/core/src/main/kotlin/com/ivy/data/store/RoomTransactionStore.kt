package com.ivy.data.store

import com.ivy.data.api.TransactionStore
import com.ivy.data.api.TagStore
import com.ivy.data.db.dao.read.TransactionDao
import com.ivy.data.db.dao.write.WriteTransactionDao
import com.ivy.data.db.entity.TransactionEntity
import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.TagId
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Transfer
import com.ivy.data.model.primitive.AssociationId
import com.ivy.data.model.primitive.NonNegativeLong
import com.ivy.data.model.primitive.toNonNegative
import com.ivy.data.mapper.TransactionMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class RoomTransactionStore @Inject constructor(
    private val mapper: TransactionMapper,
    private val transactionDao: TransactionDao,
    private val writeTransactionDao: WriteTransactionDao,
    private val tagStore: TagStore
) : TransactionStore {
    override suspend fun hasAny(): Boolean = withContext(Dispatchers.IO) {
        transactionDao.hasAny()
    }

    override suspend fun findAll(): List<Transaction> = withContext(Dispatchers.IO) {
        val tagMap = async { findAllTagAssociations() }
        retrieveTrns(
            dbCall = transactionDao::findAll,
            retrieveTags = {
                tagMap.await()[it.id] ?: emptyList()
            }
        )
    }

    override suspend fun findAllIncomeByAccount(
        accountId: AccountId
    ): List<Income> = retrieveTrns(
        dbCall = {
            transactionDao.findAllByTypeAndAccount(
                type = TransactionType.INCOME,
                accountId = accountId.value
            )
        }
    ).filterIsInstance<Income>()

    override suspend fun findAllExpenseByAccount(
        accountId: AccountId
    ): List<Expense> = retrieveTrns(
        dbCall = {
            transactionDao.findAllByTypeAndAccount(
                type = TransactionType.EXPENSE,
                accountId = accountId.value
            )
        }
    ).filterIsInstance<Expense>()

    override suspend fun findAllTransferByAccount(
        accountId: AccountId
    ): List<Transfer> = retrieveTrns(
        dbCall = {
            transactionDao.findAllByTypeAndAccount(
                type = TransactionType.TRANSFER,
                accountId = accountId.value
            )
        }
    ).filterIsInstance<Transfer>()

    override suspend fun findAllTransfersToAccount(
        toAccountId: AccountId
    ): List<Transfer> = retrieveTrns(
        dbCall = {
            transactionDao.findAllTransfersToAccount(toAccountId = toAccountId.value)
        }
    ).filterIsInstance<Transfer>()

    override suspend fun findAllBetween(
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = withContext(Dispatchers.IO) {
        val transactions = transactionDao.findAllBetween(startDate, endDate)
        val tagAssociationMap = getTagsForTransactionIds(transactions)
        transactions.mapNotNull {
            val tags = tagAssociationMap[it.id] ?: emptyList()
            with(mapper) { it.toDomain(tags = tags) }.getOrNull()
        }
    }

    override suspend fun countBetween(
        startDate: Instant,
        endDate: Instant
    ): NonNegativeLong = withContext(Dispatchers.IO) {
        transactionDao.countBetween(startDate, endDate).toNonNegative()
    }

    override suspend fun findAllByTitleMatchingPattern(pattern: String): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllByTitleMatchingPattern(pattern)
        }
    )

    override suspend fun countByTitleMatchingPattern(pattern: String): NonNegativeLong =
        withContext(Dispatchers.IO) {
            transactionDao.countByTitleMatchingPattern(pattern).toNonNegative()
        }

    override suspend fun findAllByCategory(categoryId: CategoryId): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllByCategory(categoryId.value)
        }
    )

    override suspend fun countByTitleMatchingPatternAndCategory(
        pattern: String,
        categoryId: CategoryId
    ): NonNegativeLong = withContext(Dispatchers.IO) {
        transactionDao.countByTitleMatchingPatternAndCategoryId(
            pattern = pattern,
            categoryId = categoryId.value
        ).toNonNegative()
    }

    override suspend fun findAllByAccount(accountId: AccountId): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllByAccount(accountId.value)
        }
    )

    override suspend fun countByTitleMatchingPatternAndAccount(
        pattern: String,
        accountId: AccountId
    ): NonNegativeLong = withContext(Dispatchers.IO) {
        transactionDao.countByTitleMatchingPatternAndAccountId(
            pattern = pattern,
            accountId = accountId.value
        ).toNonNegative()
    }

    override suspend fun findAllByAccountAndBetween(
        accountId: AccountId,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllByAccountAndBetween(
                accountId = accountId.value,
                startDate = startDate,
                endDate = endDate
            )
        }
    )

    override suspend fun findAllToAccountAndBetween(
        toAccountId: AccountId,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllToAccountAndBetween(
                toAccountId = toAccountId.value,
                startDate = startDate,
                endDate = endDate
            )
        }
    )

    override suspend fun findAllDueToBetween(
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllDueToBetween(
                startDate = startDate,
                endDate = endDate
            )
        }
    )

    override suspend fun findAllDueToBetweenByCategory(
        startDate: Instant,
        endDate: Instant,
        categoryId: CategoryId
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllDueToBetweenByCategory(
                startDate = startDate,
                endDate = endDate,
                categoryId = categoryId.value
            )
        }
    )

    override suspend fun findAllDueToBetweenByCategoryUnspecified(
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllDueToBetweenByCategoryUnspecified(
                startDate = startDate,
                endDate = endDate
            )
        }
    )

    override suspend fun findAllDueToBetweenByAccount(
        startDate: Instant,
        endDate: Instant,
        accountId: AccountId
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllDueToBetweenByAccount(
                startDate = startDate,
                endDate = endDate,
                accountId = accountId.value
            )
        }
    )

    override suspend fun findAllByCategoryAndTypeAndBetween(
        categoryId: UUID,
        type: TransactionType,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllByCategoryAndTypeAndBetween(
                categoryId = categoryId,
                type = type,
                startDate = startDate,
                endDate = endDate
            )
        }
    )

    override suspend fun findAllUnspecifiedAndTypeAndBetween(
        type: TransactionType,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllUnspecifiedAndTypeAndBetween(
                type = type,
                startDate = startDate,
                endDate = endDate
            )
        }
    )

    override suspend fun findAllUnspecifiedAndBetween(
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllUnspecifiedAndBetween(
                startDate = startDate,
                endDate = endDate
            )
        }
    )

    override suspend fun findAllByCategoryAndBetween(
        categoryId: UUID,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllByCategoryAndBetween(
                categoryId = categoryId,
                startDate = startDate,
                endDate = endDate
            )
        }
    )

    override suspend fun findAllByRecurringRuleId(recurringRuleId: UUID): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllByRecurringRuleId(recurringRuleId)
        }
    )

    override suspend fun findById(
        id: TransactionId
    ): Transaction? = withContext(Dispatchers.IO) {
        transactionDao.findById(id.value)?.let {
            with(mapper) { it.toDomain() }.getOrNull()
        }
    }

    override suspend fun findByIds(ids: List<TransactionId>): List<Transaction> {
        return withContext(Dispatchers.IO) {
            val tagMap = async { findTagsForTransactionIds(ids) }
            retrieveTrns(
                dbCall = {
                    transactionDao.findByIds(ids.map { it.value })
                },
                retrieveTags = {
                    tagMap.await()[it.id] ?: emptyList()
                }
            )
        }
    }

    override suspend fun save(value: Transaction) {
        withContext(Dispatchers.IO) {
            writeTransactionDao.save(
                with(mapper) { value.toEntity() }
            )
        }
    }

    override suspend fun saveMany(value: List<Transaction>) {
        withContext(Dispatchers.IO) {
            writeTransactionDao.saveMany(
                value.map { with(mapper) { it.toEntity() } }
            )
        }
    }

    override suspend fun deleteById(id: TransactionId) {
        withContext(Dispatchers.IO) {
            writeTransactionDao.deleteById(id.value)
        }
    }

    override suspend fun deleteAllByAccountId(accountId: AccountId) {
        withContext(Dispatchers.IO) {
            writeTransactionDao.deleteAllByAccountId(accountId.value)
        }
    }

    override suspend fun deletedByRecurringRuleIdAndNoDateTime(recurringRuleId: UUID) {
        withContext(Dispatchers.IO) {
            writeTransactionDao.deletedByRecurringRuleIdAndNoDateTime(recurringRuleId)
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            writeTransactionDao.deleteAll()
        }
    }

    override suspend fun countHappenedTransactions(): NonNegativeLong = withContext(Dispatchers.IO) {
        transactionDao.countHappenedTransactions().toNonNegative()
    }

    override suspend fun findLoanTransaction(loanId: UUID): Transaction? =
        withContext(Dispatchers.IO) {
            transactionDao.findLoanTransaction(loanId)?.let {
                with(mapper) { it.toDomain() }.getOrNull()
            }
        }

    override suspend fun findLoanRecordTransaction(loanRecordId: UUID): Transaction? =
        withContext(Dispatchers.IO) {
            transactionDao.findLoanRecordTransaction(loanRecordId)?.let {
                with(mapper) { it.toDomain() }.getOrNull()
            }
        }

    override suspend fun findAllByLoanId(loanId: UUID): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDao.findAllByLoanId(loanId)
        }
    )

    private suspend fun retrieveTrns(
        dbCall: suspend () -> List<TransactionEntity>,
        retrieveTags: suspend (TransactionEntity) -> List<TagId> = { emptyList() },
    ): List<Transaction> = withContext(Dispatchers.IO) {
        dbCall().mapNotNull {
            with(mapper) { it.toDomain(tags = retrieveTags(it)) }.getOrNull()
        }
    }

    private suspend fun getTagsForTransactionIds(
        transactions: List<TransactionEntity>
    ): Map<UUID, List<TagId>> {
        return findTagsForTransactionIds(transactions.map { TransactionId(it.id) })
    }

    private suspend fun findTagsForTransactionIds(
        transactionIds: List<TransactionId>
    ): Map<UUID, List<TagId>> {
        return tagStore.findByAssociatedId(transactionIds.map { AssociationId(it.value) })
            .entries.associate {
                it.key.value to it.value.map { ta -> ta.id }
            }
    }

    private suspend fun findAllTagAssociations(): Map<UUID, List<TagId>> {
        return tagStore.findByAllTagsForAssociations().entries.associate {
            it.key.value to it.value.map { ta -> ta.id }
        }
    }
}

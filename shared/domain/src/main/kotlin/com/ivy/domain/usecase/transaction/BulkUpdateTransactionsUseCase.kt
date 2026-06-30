package com.ivy.domain.usecase.transaction

import com.ivy.data.api.TagStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.TagId
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.primitive.AssociationId
import com.ivy.domain.usecase.reset.NotifyAllDataChangedUseCase
import javax.inject.Inject

/**
 * Applies the same attribute change to a whole batch of transactions at once.
 * After every batch operation a global data-change event is emitted so already
 * loaded screens (home, accounts, ...) refresh their numbers.
 */
class BulkUpdateTransactionsUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
    private val tagStore: TagStore,
    private val notifyAllDataChangedUseCase: NotifyAllDataChangedUseCase,
) {
    /**
     * Sets [categoryId] (null means "uncategorized") on every given transaction.
     */
    suspend fun updateCategory(transactions: List<Transaction>, categoryId: CategoryId?) {
        if (transactions.isEmpty()) return
        transactionStore.saveMany(transactions.map { it.withCategory(categoryId) })
        notifyAllDataChangedUseCase()
    }

    /**
     * Moves income/expense transactions to [accountId]. Transfers reference two
     * accounts so they are skipped; the number of skipped transfers is returned.
     */
    suspend fun updateAccount(transactions: List<Transaction>, accountId: AccountId): Int {
        if (transactions.isEmpty()) return 0
        val (transfers, movable) = transactions.partition { it is Transfer }
        if (movable.isNotEmpty()) {
            transactionStore.saveMany(movable.map { it.withAccount(accountId) })
            notifyAllDataChangedUseCase()
        }
        return transfers.size
    }

    suspend fun addTag(transactions: List<Transaction>, tagId: TagId) {
        if (transactions.isEmpty()) return
        transactions.forEach {
            tagStore.associateTagToEntity(AssociationId(it.id.value), tagId)
        }
        notifyAllDataChangedUseCase()
    }

    suspend fun removeTag(transactions: List<Transaction>, tagId: TagId) {
        if (transactions.isEmpty()) return
        transactions.forEach {
            tagStore.removeTagAssociation(AssociationId(it.id.value), tagId)
        }
        notifyAllDataChangedUseCase()
    }
}

private fun Transaction.withCategory(categoryId: CategoryId?): Transaction = when (this) {
    is Income -> copy(category = categoryId)
    is Expense -> copy(category = categoryId)
    is Transfer -> copy(category = categoryId)
}

private fun Transaction.withAccount(accountId: AccountId): Transaction = when (this) {
    is Income -> copy(account = accountId)
    is Expense -> copy(account = accountId)
    is Transfer -> this
}

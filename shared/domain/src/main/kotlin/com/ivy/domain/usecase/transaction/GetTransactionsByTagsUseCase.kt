package com.ivy.domain.usecase.transaction

import com.ivy.data.api.TagStore
import com.ivy.data.model.TagId
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.api.TransactionStore
import javax.inject.Inject

class GetTransactionsByTagsUseCase @Inject internal constructor(
    private val tagStore: TagStore,
    private val transactionStore: TransactionStore
) {
    suspend operator fun invoke(tagIds: List<TagId>): List<Transaction> {
        val transactionIds = tagStore.findByAllAssociatedIdForTagId(tagIds)
            .asSequence()
            .flatMap { it.value }
            .map { TransactionId(it.associatedId.value) }
            .distinct()
            .toList()

        return transactionStore.findByIds(transactionIds)
    }
}

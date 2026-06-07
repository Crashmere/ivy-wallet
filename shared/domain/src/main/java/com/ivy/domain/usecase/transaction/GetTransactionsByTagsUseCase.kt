package com.ivy.domain.usecase.transaction

import com.ivy.data.model.TagId
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.repository.TagRepository
import com.ivy.data.repository.TransactionRepository
import javax.inject.Inject

class GetTransactionsByTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(tagIds: List<TagId>): List<Transaction> {
        val transactionIds = tagRepository.findByAllAssociatedIdForTagId(tagIds)
            .asSequence()
            .flatMap { it.value }
            .map { TransactionId(it.associatedId.value) }
            .distinct()
            .toList()

        return transactionRepository.findByIds(transactionIds)
    }
}

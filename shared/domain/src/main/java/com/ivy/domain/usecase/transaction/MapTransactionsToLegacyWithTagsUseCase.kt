package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.LegacyTransaction
import com.ivy.data.model.Transaction
import com.ivy.data.repository.TagRepository
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.legacy.domain.mapper.toImmutableLegacyTags
import com.ivy.legacy.domain.mapper.toLegacyDomain
import javax.inject.Inject

class MapTransactionsToLegacyWithTagsUseCase @Inject constructor(
    private val transactionMapper: TransactionMapper,
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(transactions: List<Transaction>): List<LegacyTransaction> {
        return transactions.map {
            val tags = tagRepository.findByIds(it.tags).toImmutableLegacyTags()
            with(transactionMapper) {
                it.toEntity().toLegacyDomain(tags = tags)
            }
        }
    }
}

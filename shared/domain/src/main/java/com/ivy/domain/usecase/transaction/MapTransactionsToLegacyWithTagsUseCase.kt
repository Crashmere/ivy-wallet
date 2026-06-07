package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.LegacyTransaction
import com.ivy.data.api.TagStore
import com.ivy.data.model.Transaction
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.domain.mapper.legacy.toImmutableLegacyTags
import com.ivy.domain.mapper.legacy.toLegacyDomain
import javax.inject.Inject

class MapTransactionsToLegacyWithTagsUseCase @Inject constructor(
    private val transactionMapper: TransactionMapper,
    private val tagStore: TagStore
) {
    suspend operator fun invoke(transactions: List<Transaction>): List<LegacyTransaction> {
        return transactions.map {
            val tags = tagStore.findByIds(it.tags).toImmutableLegacyTags()
            with(transactionMapper) {
                it.toEntity().toLegacyDomain(tags = tags)
            }
        }
    }
}

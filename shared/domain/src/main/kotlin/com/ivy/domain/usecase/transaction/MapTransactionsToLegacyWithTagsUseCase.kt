package com.ivy.domain.usecase.transaction

import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.api.TagStore
import com.ivy.data.model.Transaction
import com.ivy.domain.mapper.legacy.toImmutableLegacyTags
import com.ivy.domain.mapper.legacy.toLegacyTransaction
import javax.inject.Inject

class MapTransactionsToLegacyWithTagsUseCase @Inject constructor(
    private val tagStore: TagStore
) {
    suspend operator fun invoke(transactions: List<Transaction>): List<LegacyTransaction> {
        return transactions.map {
            val tags = tagStore.findByIds(it.tags).toImmutableLegacyTags()
            it.toLegacyTransaction(tags)
        }
    }
}

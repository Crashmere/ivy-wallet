package com.ivy.domain.usecase.tag

import com.ivy.data.api.TagStore
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.data.model.primitive.AssociationId
import java.util.UUID
import javax.inject.Inject

class GetTransactionTagIdsUseCase @Inject constructor(
    private val tagStore: TagStore
) {
    suspend operator fun invoke(transactionId: UUID): List<TagId> {
        return tagStore.findByAssociatedId(AssociationId(transactionId))
            .map(Tag::id)
    }
}

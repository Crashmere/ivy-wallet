package com.ivy.domain.usecase.tag

import com.ivy.data.api.TagStore
import com.ivy.data.model.TagId
import com.ivy.data.model.primitive.AssociationId
import java.util.UUID
import javax.inject.Inject

class CopyTagsToTransactionUseCase @Inject constructor(
    private val tagStore: TagStore
) {
    suspend operator fun invoke(tagIds: List<TagId>, transactionId: UUID) {
        tagStore.findByIds(tagIds).forEach { tag ->
            tagStore.associateTagToEntity(
                associationId = AssociationId(transactionId),
                tagId = tag.id
            )
        }
    }
}

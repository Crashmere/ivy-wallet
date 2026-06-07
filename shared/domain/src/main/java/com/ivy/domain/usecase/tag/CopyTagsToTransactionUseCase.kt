package com.ivy.domain.usecase.tag

import com.ivy.data.model.TagId
import com.ivy.data.model.primitive.AssociationId
import com.ivy.data.repository.TagRepository
import java.util.UUID
import javax.inject.Inject

class CopyTagsToTransactionUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(tagIds: List<TagId>, transactionId: UUID) {
        tagRepository.findByIds(tagIds).forEach { tag ->
            tagRepository.associateTagToEntity(
                associationId = AssociationId(transactionId),
                tagId = tag.id
            )
        }
    }
}

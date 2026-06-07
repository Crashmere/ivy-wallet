package com.ivy.domain.usecase.tag

import com.ivy.data.model.TagId
import com.ivy.data.model.primitive.AssociationId
import com.ivy.data.repository.TagRepository
import java.util.UUID
import javax.inject.Inject

class AssociateTagToTransactionUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(transactionId: UUID, tagId: TagId) {
        tagRepository.associateTagToEntity(AssociationId(transactionId), tagId)
    }
}

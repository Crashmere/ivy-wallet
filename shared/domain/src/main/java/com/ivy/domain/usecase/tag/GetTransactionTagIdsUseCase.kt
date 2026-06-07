package com.ivy.domain.usecase.tag

import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.data.model.primitive.AssociationId
import com.ivy.data.repository.TagRepository
import java.util.UUID
import javax.inject.Inject

class GetTransactionTagIdsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(transactionId: UUID): List<TagId> {
        return tagRepository.findByAssociatedId(AssociationId(transactionId))
            .map(Tag::id)
    }
}

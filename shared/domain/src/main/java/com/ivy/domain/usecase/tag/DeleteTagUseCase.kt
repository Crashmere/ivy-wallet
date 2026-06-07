package com.ivy.domain.usecase.tag

import com.ivy.data.model.TagId
import com.ivy.data.repository.TagRepository
import javax.inject.Inject

class DeleteTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(tagId: TagId) {
        tagRepository.deleteById(tagId)
    }
}

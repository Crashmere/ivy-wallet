package com.ivy.domain.usecase.tag

import com.ivy.data.model.Tag
import com.ivy.data.repository.TagRepository
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(): List<Tag> {
        return tagRepository.findAll()
    }
}

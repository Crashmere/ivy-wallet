package com.ivy.domain.usecase.tag

import com.ivy.data.model.Tag
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.repository.TagRepository
import com.ivy.data.repository.mapper.TagMapper
import javax.inject.Inject

class CreateTagUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val tagMapper: TagMapper
) {
    suspend operator fun invoke(name: NotBlankTrimmedString): Tag {
        val tag = with(tagMapper) { createNewTag(name = name) }
        tagRepository.save(tag)
        return tag
    }
}

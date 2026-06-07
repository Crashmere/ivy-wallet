package com.ivy.domain.usecase.tag

import com.ivy.data.api.TagStore
import com.ivy.data.model.Tag
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.repository.mapper.TagMapper
import javax.inject.Inject

class CreateTagUseCase @Inject constructor(
    private val tagStore: TagStore,
    private val tagMapper: TagMapper
) {
    suspend operator fun invoke(name: NotBlankTrimmedString): Tag {
        val tag = with(tagMapper) { createNewTag(name = name) }
        tagStore.save(tag)
        return tag
    }
}

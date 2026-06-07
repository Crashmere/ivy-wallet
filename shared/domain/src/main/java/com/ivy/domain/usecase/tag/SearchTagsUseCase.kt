package com.ivy.domain.usecase.tag

import com.ivy.data.model.Tag
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.repository.TagRepository
import javax.inject.Inject

class SearchTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(query: NotBlankTrimmedString): List<Tag> {
        return tagRepository.findByText(text = query.value)
    }
}

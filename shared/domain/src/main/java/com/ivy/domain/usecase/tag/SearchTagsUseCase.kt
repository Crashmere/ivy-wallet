package com.ivy.domain.usecase.tag

import com.ivy.data.api.TagStore
import com.ivy.data.model.Tag
import com.ivy.data.model.primitive.NotBlankTrimmedString
import javax.inject.Inject

class SearchTagsUseCase @Inject constructor(
    private val tagStore: TagStore
) {
    suspend operator fun invoke(query: NotBlankTrimmedString): List<Tag> {
        return tagStore.findByText(text = query.value)
    }
}

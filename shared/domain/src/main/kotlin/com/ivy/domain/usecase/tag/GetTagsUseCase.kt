package com.ivy.domain.usecase.tag

import com.ivy.data.api.TagStore
import com.ivy.data.model.Tag
import javax.inject.Inject

class GetTagsUseCase @Inject internal constructor(
    private val tagStore: TagStore
) {
    suspend operator fun invoke(): List<Tag> {
        return tagStore.findAll()
    }
}

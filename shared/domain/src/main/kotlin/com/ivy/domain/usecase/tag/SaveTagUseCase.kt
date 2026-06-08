package com.ivy.domain.usecase.tag

import com.ivy.data.api.TagStore
import com.ivy.data.model.Tag
import javax.inject.Inject

class SaveTagUseCase @Inject internal constructor(
    private val tagStore: TagStore
) {
    suspend operator fun invoke(tag: Tag) {
        tagStore.save(tag)
    }
}

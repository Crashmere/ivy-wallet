package com.ivy.domain.usecase.tag

import com.ivy.data.api.TagStore
import com.ivy.data.model.TagId
import javax.inject.Inject

class DeleteTagUseCase @Inject internal constructor(
    private val tagStore: TagStore
) {
    suspend operator fun invoke(tagId: TagId) {
        tagStore.deleteById(tagId)
    }
}

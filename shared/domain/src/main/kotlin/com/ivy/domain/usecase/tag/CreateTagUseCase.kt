package com.ivy.domain.usecase.tag

import com.ivy.data.api.TagStore
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.NotBlankTrimmedString
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class CreateTagUseCase @Inject internal constructor(
    private val tagStore: TagStore,
) {
    suspend operator fun invoke(name: NotBlankTrimmedString): Tag {
        val tag = Tag(
            id = TagId(UUID.randomUUID()),
            name = name,
            description = null,
            color = ColorInt(TRANSPARENT_COLOR),
            icon = null,
            orderNum = 0.0,
            creationTimestamp = Instant.now(),
        )
        tagStore.save(tag)
        return tag
    }

    private companion object {
        const val TRANSPARENT_COLOR = 0x00000000
    }
}

package com.ivy.data.mapper

import arrow.core.Either
import arrow.core.raise.either
import com.ivy.data.db.entity.TagAssociationEntity
import com.ivy.data.db.entity.TagEntity
import com.ivy.data.model.Tag
import com.ivy.data.model.TagAssociation
import com.ivy.data.model.primitive.AssociationId
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.model.TagId
import javax.inject.Inject

class TagMapper @Inject constructor() {
    internal fun TagEntity.toDomain(): Either<String, Tag> = either {
        Tag(
            id = TagId(id),
            name = NotBlankTrimmedString.from(name).bind(),
            description = description,
            color = ColorInt(color),
            icon = icon?.let(IconAsset::from)?.getOrNull(),
            orderNum = orderNum,
            creationTimestamp = dateTime,
        )
    }

    internal fun Tag.toEntity(): TagEntity {
        return TagEntity(
            id = id.value,
            name = name.value,
            description = description,
            color = color.value,
            icon = icon?.id,
            orderNum = orderNum,
            dateTime = creationTimestamp,
            isDeleted = false,
        )
    }

    internal fun TagAssociation.toEntity(): TagAssociationEntity {
        return TagAssociationEntity(
            tagId = id.value,
            associatedId = associatedId.value,
            isDeleted = false,
        )
    }

    internal fun TagAssociationEntity.toDomain(): TagAssociation {
        return TagAssociation(
            id = TagId(tagId),
            associatedId = AssociationId(associatedId),
        )
    }

    internal fun createNewTagAssociation(tagId: TagId, associationId: AssociationId): TagAssociation {
        return TagAssociation(
            id = tagId,
            associatedId = associationId,
        )
    }
}

package com.ivy.data.model

import com.ivy.data.model.primitive.AssociationId
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.model.identity.Identifiable
import com.ivy.data.model.identity.UniqueId
import java.time.Instant
import java.util.UUID

data class Tag(
    override val id: TagId,
    val name: NotBlankTrimmedString,
    val description: String?,
    val color: ColorInt,
    val icon: IconAsset?,
    val orderNum: Double,
    val creationTimestamp: Instant,
) : Identifiable<TagId>

@Suppress("DataClassTypedIDs")
data class TagAssociation(
    override val id: TagId,
    val associatedId: AssociationId,
) : Identifiable<TagId>

@JvmInline
value class TagId(override val value: UUID) : UniqueId

package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import com.ivy.data.db.serializer.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@Entity(tableName = "tags_association", primaryKeys = ["tagId", "associatedId"])
internal data class TagAssociationEntity(
    @SerialName("tagId")
    @Serializable(with = KSerializerUUID::class)
    val tagId: UUID,

    @SerialName("associatedId")
    @Serializable(with = KSerializerUUID::class)
    val associatedId: UUID,

    @SerialName("isDeleted")
    val isDeleted: Boolean
)

package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.data.db.serializer.KSerializerInstant
import com.ivy.data.db.serializer.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Keep
@Serializable
@Entity(tableName = "tags")
internal data class TagEntity(
    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID,

    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String?,

    @SerialName("color")
    val color: Int,
    @SerialName("icon")
    val icon: String?,
    @SerialName("orderNum")
    val orderNum: Double,

    @SerialName("creationTime")
    @Serializable(with = KSerializerInstant::class)
    val dateTime: Instant,

    @SerialName("isDeleted")
    val isDeleted: Boolean
)

package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.data.db.serializer.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Suppress("DataClassDefaultValues")
@Keep
@Serializable
@Entity(tableName = "accounts")
internal data class AccountEntity(
    @SerialName("name")
    val name: String,
    @SerialName("currency")
    val currency: String? = null,
    @SerialName("color")
    val color: Int,
    @SerialName("icon")
    val icon: String? = null,
    @SerialName("orderNum")
    val orderNum: Double = 0.0,
    @SerialName("includeInBalance")
    val includeInBalance: Boolean = true,

    // Comma-separated CategoryId UUIDs that make up this account's own category list.
    // null/empty => the account has no categories assigned yet (picker shows none by default).
    @SerialName("visibleCategoryIdsSerialized")
    val visibleCategoryIdsSerialized: String? = null,

    @SerialName("isDeleted")
    val isDeleted: Boolean = false,

    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID()
)

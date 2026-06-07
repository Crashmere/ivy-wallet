package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserialization.KSerializerUUID
import com.ivy.base.theme.Theme
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Suppress("DataClassDefaultValues")
@Deprecated("Legacy concept - migrate to DataStore and get rid of it.")
@Keep
@Serializable
@Entity(tableName = "settings")
data class SettingsEntity(
    @SerialName("theme")
    val theme: Theme,
    @SerialName("currency")
    val currency: String,
    @SerialName("bufferAmount")
    val bufferAmount: Double,

    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID()
)

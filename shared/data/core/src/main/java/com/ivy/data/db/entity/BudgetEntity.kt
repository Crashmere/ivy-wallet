package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserialization.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Suppress("DataClassDefaultValues")
@Keep
@Serializable
@Entity(tableName = "budgets")
data class BudgetEntity(
    @SerialName("name")
    val name: String,
    @SerialName("amount")
    val amount: Double,

    @SerialName("categoryIdsSerialized")
    val categoryIdsSerialized: String?,
    @SerialName("accountIdsSerialized")
    val accountIdsSerialized: String?,

    @SerialName("isDeleted")
    val isDeleted: Boolean = false,

    @SerialName("orderId")
    val orderId: Double,
    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID()
)

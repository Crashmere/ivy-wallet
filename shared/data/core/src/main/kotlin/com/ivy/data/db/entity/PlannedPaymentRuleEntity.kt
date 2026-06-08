package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.data.db.serializer.KSerializerInstant
import com.ivy.data.db.serializer.KSerializerUUID
import com.ivy.data.model.TransactionType
import com.ivy.data.model.IntervalType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Suppress("DataClassDefaultValues")
@Keep
@Serializable
@Entity(tableName = "planned_payment_rules")
internal data class PlannedPaymentRuleEntity(
    @SerialName("startDate")
    @Serializable(with = KSerializerInstant::class)
    val startDate: Instant?,
    @SerialName("intervalN")
    val intervalN: Int?,
    @SerialName("intervalType")
    val intervalType: IntervalType?,
    @SerialName("oneTime")
    val oneTime: Boolean,
    @SerialName("type")
    val type: TransactionType,
    @SerialName("accountId")
    @Serializable(with = KSerializerUUID::class)
    val accountId: UUID,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("categoryId")
    @Serializable(with = KSerializerUUID::class)
    val categoryId: UUID? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("description")
    val description: String? = null,

    @SerialName("isDeleted")
    val isDeleted: Boolean = false,

    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID()
)

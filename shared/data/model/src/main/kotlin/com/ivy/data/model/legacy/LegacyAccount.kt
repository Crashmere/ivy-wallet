package com.ivy.data.model.legacy

import arrow.core.Either
import arrow.core.raise.either
import com.ivy.data.model.AccountId
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import java.util.UUID
import com.ivy.data.model.Account as DomainAccount

data class LegacyAccount(
    val name: String,
    val color: Int,
    val currency: String? = null,
    val icon: String? = null,
    val orderNum: Double = 0.0,
    val includeInBalance: Boolean = true,

    val isDeleted: Boolean = false,

    val id: UUID = UUID.randomUUID()
) {
    @Suppress("DataClassFunctions")
    fun toDomainAccount(
        baseCurrency: AssetCode
    ): Either<String, DomainAccount> {
        return either {
            DomainAccount(
                id = AccountId(id),
                name = NotBlankTrimmedString.from(name).bind(),
                asset = currency?.let(AssetCode::from)?.bind()
                    ?: baseCurrency,
                color = ColorInt(color),
                icon = icon?.let(IconAsset::from)?.getOrNull(),
                includeInBalance = includeInBalance,
                orderNum = orderNum,
            )
        }
    }
}

package com.ivy.data.mapper

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.ivy.data.api.CurrencyStore
import com.ivy.data.db.entity.AccountEntity
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import java.util.UUID
import javax.inject.Inject

internal class AccountMapper @Inject internal constructor(
    private val currencyStore: CurrencyStore
) {
    internal suspend fun AccountEntity.toDomain(): Either<String, Account> = either {
        ensure(!isDeleted) { "Account is deleted" }

        Account(
            id = AccountId(id),
            name = NotBlankTrimmedString.from(name).bind(),
            asset = currency?.let(AssetCode::from)?.getOrNull()
                ?: currencyStore.getBaseCurrency(),
            color = ColorInt(color),
            icon = icon?.let(IconAsset::from)?.getOrNull(),
            includeInBalance = includeInBalance,
            orderNum = orderNum,
            visibleCategories = parseCategoryIds(visibleCategoryIdsSerialized),
        )
    }

    internal fun Account.toEntity(): AccountEntity {
        return AccountEntity(
            name = name.value,
            currency = asset.code,
            color = color.value,
            icon = icon?.id,
            orderNum = orderNum,
            includeInBalance = includeInBalance,
            visibleCategoryIdsSerialized = visibleCategories
                .takeIf { it.isNotEmpty() }
                ?.joinToString(separator = ",") { it.value.toString() },
            id = id.value,
        )
    }

    private fun parseCategoryIds(serialized: String?): List<CategoryId> {
        if (serialized.isNullOrBlank()) return emptyList()
        return serialized.split(",").mapNotNull { raw ->
            runCatching { CategoryId(UUID.fromString(raw.trim())) }.getOrNull()
        }
    }
}

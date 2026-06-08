package com.ivy.domain.mapper.legacy

import arrow.core.Either
import arrow.core.raise.either
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString

fun Account.toLegacyDomain(): LegacyAccount = LegacyAccount(
    name = name.value,
    currency = asset.code,
    color = color.value,
    icon = icon?.id,
    orderNum = orderNum,
    includeInBalance = includeInBalance,
    isDeleted = false,
    id = id.value
)

fun LegacyAccount.toDomainAccount(
    baseCurrency: AssetCode
): Either<String, Account> = either {
    Account(
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

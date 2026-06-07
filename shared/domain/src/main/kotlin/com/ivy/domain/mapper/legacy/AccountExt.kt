package com.ivy.domain.mapper.legacy

import com.ivy.data.model.Account
import com.ivy.data.model.legacy.Account as LegacyAccount

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

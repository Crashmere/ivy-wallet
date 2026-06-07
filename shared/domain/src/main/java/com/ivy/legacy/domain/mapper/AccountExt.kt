package com.ivy.legacy.domain.mapper

import com.ivy.data.db.entity.AccountEntity
import com.ivy.data.model.legacy.Account

fun AccountEntity.toLegacyDomain(): Account = Account(
    name = name,
    currency = currency,
    color = color,
    icon = icon,
    orderNum = orderNum,
    includeInBalance = includeInBalance,
    isDeleted = isDeleted,
    id = id
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    name = name,
    currency = currency,
    color = color,
    icon = icon,
    orderNum = orderNum,
    includeInBalance = includeInBalance,
    isDeleted = isDeleted,
    id = id
)

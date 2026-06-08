package com.ivy.transactions

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Account
import com.ivy.data.model.legacy.LegacyAccount
import java.util.UUID

@Immutable
internal data class TransactionsAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val currency: String?,
    val icon: String?,
    val includeInBalance: Boolean,
)

internal fun Account.toTransactionsAccount() = TransactionsAccount(
    id = id.value,
    name = name.value,
    color = color.value,
    currency = asset.code,
    icon = icon?.id,
    includeInBalance = includeInBalance,
)

internal fun TransactionsAccount.toLegacyAccount() = LegacyAccount(
    id = id,
    name = name,
    color = color,
    currency = currency,
    icon = icon,
    includeInBalance = includeInBalance,
)

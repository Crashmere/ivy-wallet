package com.ivy.transactions

import androidx.compose.runtime.Immutable
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

internal fun LegacyAccount.toTransactionsAccount() = TransactionsAccount(
    id = id,
    name = name,
    color = color,
    currency = currency,
    icon = icon,
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

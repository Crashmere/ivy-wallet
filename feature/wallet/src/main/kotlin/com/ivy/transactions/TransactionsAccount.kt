package com.ivy.transactions

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Account
import com.ivy.ui.modal.AccountModalAccount
import java.util.UUID

@Immutable
internal data class TransactionsAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val currency: String?,
    val icon: String?,
    val includeInBalance: Boolean,
    val visibleCategoryIds: Set<UUID> = emptySet(),
)

internal fun Account.toTransactionsAccount() = TransactionsAccount(
    id = id.value,
    name = name.value,
    color = color.value,
    currency = asset.code,
    icon = icon?.id,
    includeInBalance = includeInBalance,
    visibleCategoryIds = visibleCategories.mapTo(mutableSetOf()) { it.value },
)

internal fun TransactionsAccount.toAccountModalAccount() = AccountModalAccount(
    id = id,
    name = name,
    color = color,
    currency = currency,
    icon = icon,
    includeInBalance = includeInBalance,
    visibleCategoryIds = visibleCategoryIds,
)

package com.ivy.legacy.ui.transaction

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Account
import com.ivy.data.model.Category
import com.ivy.data.model.legacy.LegacyAccount
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

@Immutable
data class TransactionListData(
    val baseCurrency: String,
    val accounts: ImmutableList<TransactionListAccount>,
    val categories: ImmutableList<Category>
)

@Immutable
data class TransactionListAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
    val currency: String?,
)

fun Account.toTransactionListAccount() = TransactionListAccount(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
    currency = asset.code,
)

fun LegacyAccount.toTransactionListAccount() = TransactionListAccount(
    id = id,
    name = name,
    color = color,
    icon = icon,
    currency = currency,
)

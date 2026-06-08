package com.ivy.transaction

import androidx.compose.runtime.Immutable
import com.ivy.data.model.legacy.LegacyAccount
import java.util.UUID

@Immutable
internal data class EditTransactionAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
    val currency: String?,
)

internal fun LegacyAccount.toEditTransactionAccount() = EditTransactionAccount(
    id = id,
    name = name,
    color = color,
    icon = icon,
    currency = currency,
)

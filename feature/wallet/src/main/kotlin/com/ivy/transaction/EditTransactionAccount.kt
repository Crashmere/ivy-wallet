package com.ivy.transaction

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Account
import java.util.UUID

@Immutable
internal data class EditTransactionAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
    val currency: String?,
    // Categories that belong to this account (its own list). Drives the category picker filtering.
    val visibleCategoryIds: Set<UUID>,
)

internal fun Account.toEditTransactionAccount() = EditTransactionAccount(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
    currency = asset.code,
    visibleCategoryIds = visibleCategories.mapTo(mutableSetOf()) { it.value },
)

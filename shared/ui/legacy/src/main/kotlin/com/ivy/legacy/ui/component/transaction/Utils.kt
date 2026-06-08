package com.ivy.legacy.ui.component.transaction

import androidx.compose.runtime.Composable
import com.ivy.data.model.Category
import com.ivy.data.model.legacy.LegacyAccount
import java.util.UUID

@Composable
fun category(
    categoryId: UUID?,
    categories: List<Category>
): Category? {
    val targetId = categoryId ?: return null
    return categories.find { it.id.value == targetId }
}

@Composable
fun account(
    accountId: UUID?,
    accounts: List<LegacyAccount>
): LegacyAccount? {
    val targetId = accountId ?: return null
    return accounts.find { it.id == targetId }
}

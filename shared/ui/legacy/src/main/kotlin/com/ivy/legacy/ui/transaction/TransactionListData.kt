package com.ivy.legacy.ui.transaction

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Category
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

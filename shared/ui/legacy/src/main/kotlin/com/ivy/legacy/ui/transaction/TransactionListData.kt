package com.ivy.legacy.ui.transaction

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Category
import java.util.UUID

@Immutable
data class TransactionListData(
    val baseCurrency: String,
    val accounts: List<TransactionListAccount>,
    val categories: List<Category>
)

@Immutable
data class TransactionListAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
    val currency: String?,
)

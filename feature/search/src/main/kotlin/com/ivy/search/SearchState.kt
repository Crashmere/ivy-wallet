package com.ivy.search

import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.Category
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

internal data class SearchState(
    val searchQuery: String,
    val transactions: ImmutableList<TransactionHistoryItem>,
    val baseCurrency: String,
    val accounts: ImmutableList<SearchAccount>,
    val categories: ImmutableList<Category>,
    val shouldShowAccountSpecificColorInTransactions: Boolean
)

internal data class SearchAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
    val currency: String?,
)

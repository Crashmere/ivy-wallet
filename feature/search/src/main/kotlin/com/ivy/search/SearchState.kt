package com.ivy.search

import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.Category
import com.ivy.data.model.legacy.LegacyAccount
import kotlinx.collections.immutable.ImmutableList

data class SearchState(
    val searchQuery: String,
    val transactions: ImmutableList<TransactionHistoryItem>,
    val baseCurrency: String,
    val accounts: ImmutableList<LegacyAccount>,
    val categories: ImmutableList<Category>,
    val shouldShowAccountSpecificColorInTransactions: Boolean
)

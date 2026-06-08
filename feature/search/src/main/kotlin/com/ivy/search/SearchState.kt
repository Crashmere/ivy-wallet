package com.ivy.search

import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.Category
import com.ivy.legacy.ui.transaction.TransactionListAccount
import kotlinx.collections.immutable.ImmutableList

internal data class SearchState(
    val searchQuery: String,
    val transactions: ImmutableList<TransactionHistoryItem>,
    val baseCurrency: String,
    val accounts: ImmutableList<TransactionListAccount>,
    val categories: ImmutableList<Category>,
    val shouldShowAccountSpecificColorInTransactions: Boolean
)

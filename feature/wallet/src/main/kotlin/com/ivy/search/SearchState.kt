package com.ivy.search

import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.Category
import com.ivy.data.model.Tag
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import java.util.UUID

internal enum class SearchTimeFilter {
    ALL,
    THIS_MONTH,
    LAST_MONTH,
    LAST_3_MONTHS,
    THIS_YEAR
}

internal data class SearchState(
    val searchQuery: String,
    val transactions: ImmutableList<TransactionHistoryItem>,
    val baseCurrency: String,
    val accounts: ImmutableList<SearchAccount>,
    val categories: ImmutableList<Category>,
    val tags: ImmutableList<Tag>,
    val selectedCategoryIds: ImmutableSet<UUID>,
    val uncategorizedSelected: Boolean,
    val selectedAccountIds: ImmutableSet<UUID>,
    val selectedTagIds: ImmutableSet<UUID>,
    val timeFilter: SearchTimeFilter,
    val shouldShowAccountSpecificColorInTransactions: Boolean
)

internal data class SearchAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
    val currency: String?,
)

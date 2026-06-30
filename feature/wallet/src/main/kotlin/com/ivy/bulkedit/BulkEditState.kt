package com.ivy.bulkedit

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Account
import com.ivy.data.model.Category
import com.ivy.data.model.Tag
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.ui.period.TimePeriod
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import java.util.UUID

@Immutable
internal data class BulkEditState(
    val baseCurrency: String,
    val period: TimePeriod,

    // Filter options derived from the transactions inside the selected period.
    val filterCategories: ImmutableList<Category>,
    val filterHasUncategorized: Boolean,
    val filterTags: ImmutableList<Tag>,

    // Full catalogs, used as the "new value" choices when applying a bulk change.
    val allCategories: ImmutableList<Category>,
    val allAccounts: ImmutableList<Account>,
    val allTags: ImmutableList<Tag>,

    // Current filter selection.
    val selectedCategoryIds: ImmutableSet<UUID>,
    val uncategorizedSelected: Boolean,
    val selectedTagIds: ImmutableSet<UUID>,

    // Preview of the matching transactions.
    val matchingTransactions: ImmutableList<TransactionHistoryItem>,
    val matchingCount: Int,
    val income: Double,
    val expenses: Double,

    val shouldShowAccountColorsInTransactions: Boolean,
    val loading: Boolean,
) {
    val isFilterActive: Boolean
        get() = selectedCategoryIds.isNotEmpty() ||
                uncategorizedSelected ||
                selectedTagIds.isNotEmpty()
}

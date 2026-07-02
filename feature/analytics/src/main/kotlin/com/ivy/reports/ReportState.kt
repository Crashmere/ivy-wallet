package com.ivy.reports

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Account
import com.ivy.data.model.Category
import com.ivy.data.model.Tag
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.ui.period.TimePeriod
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import java.util.UUID

internal enum class SortOrder { TIME, AMOUNT }

@Immutable
internal data class ReportState(
    val baseCurrency: String,
    val period: TimePeriod,
    val sortOrder: SortOrder,

    val includeIncome: Boolean,
    val includeExpense: Boolean,
    val includeTransfer: Boolean,

    val filterCategories: ImmutableList<Category>,
    val filterHasUncategorized: Boolean,
    val filterAccounts: ImmutableList<Account>,
    val filterTags: ImmutableList<Tag>,

    val selectedCategoryIds: ImmutableSet<UUID>,
    val uncategorizedSelected: Boolean,
    val selectedAccountIds: ImmutableSet<UUID>,
    val selectedTagIds: ImmutableSet<UUID>,

    val includeKeywords: List<String>,
    val excludeKeywords: List<String>,
    val amountMin: Float?,
    val amountMax: Float?,
    val amountRangeMin: Float,
    val amountRangeMax: Float,

    val matchingTransactions: ImmutableList<TransactionHistoryItem>,
    val matchingCount: Int,
    val income: Double,
    val expenses: Double,

    val allCategories: ImmutableList<Category>,
    val allAccounts: ImmutableList<Account>,
    val allTags: ImmutableList<Tag>,

    val shouldShowAccountColorsInTransactions: Boolean,
    val loading: Boolean,
    val advancedExpanded: Boolean,
) {
    val isFilterActive: Boolean
        get() = selectedCategoryIds.isNotEmpty() ||
                uncategorizedSelected ||
                selectedAccountIds.isNotEmpty() ||
                selectedTagIds.isNotEmpty() ||
                !includeIncome ||
                !includeExpense ||
                includeTransfer ||
                includeKeywords.isNotEmpty() ||
                excludeKeywords.isNotEmpty() ||
                amountMin != null ||
                amountMax != null
}

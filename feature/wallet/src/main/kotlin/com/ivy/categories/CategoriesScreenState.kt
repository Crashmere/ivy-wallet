package com.ivy.categories

import androidx.compose.runtime.Immutable
import com.ivy.ui.period.TimePeriod
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.util.UUID

internal data class CategoriesScreenState(
    val baseCurrency: String = "",
    val categories: ImmutableList<CategoryData> = persistentListOf(),
    val accounts: ImmutableList<CategoryAccountHeader> = persistentListOf(),
    val reorderModalVisible: Boolean = false,
    val sortModalVisible: Boolean = false,
    val sortOrderItems: ImmutableList<SortOrder> = SortOrder.values().toList().toImmutableList(),
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val compactCategoriesModeEnabled: Boolean,
    val showCategorySearchBar: Boolean,
    val period: TimePeriod = TimePeriod(),
)

/** An account plus the ids of the categories it owns, used to group the categories list. */
@Immutable
internal data class CategoryAccountHeader(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
    val orderNum: Double,
    val categoryIds: Set<UUID>,
)

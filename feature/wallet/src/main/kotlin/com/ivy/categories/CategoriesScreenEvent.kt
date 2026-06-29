package com.ivy.categories

import com.ivy.data.model.CreateCategoryData
import com.ivy.ui.period.TimePeriod

internal sealed interface CategoriesScreenEvent {
    data class OnReorder(
        val newOrder: List<CategoryData>,
        val sortOrder: SortOrder = SortOrder.DEFAULT
    ) : CategoriesScreenEvent

    data class OnCreateCategory(val createCategoryData: CreateCategoryData) :
        CategoriesScreenEvent

    data class OnReorderModalVisible(val visible: Boolean) : CategoriesScreenEvent
    data class OnSortOrderModalVisible(val visible: Boolean) : CategoriesScreenEvent
    data class OnSearchQueryUpdate(val queryString: String) : CategoriesScreenEvent

    data object OnNextMonth : CategoriesScreenEvent
    data object OnPreviousMonth : CategoriesScreenEvent
    data class OnSelectPeriod(val period: TimePeriod) : CategoriesScreenEvent
}

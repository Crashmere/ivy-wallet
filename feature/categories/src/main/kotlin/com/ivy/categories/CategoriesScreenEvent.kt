package com.ivy.categories

import com.ivy.data.model.CreateCategoryData
import com.ivy.ui.modal.CategoryModalData

internal sealed interface CategoriesScreenEvent {
    data class OnReorder(
        val newOrder: List<CategoryData>,
        val sortOrder: SortOrder = SortOrder.DEFAULT
    ) : CategoriesScreenEvent

    data class OnCreateCategory(val createCategoryData: CreateCategoryData) :
        CategoriesScreenEvent

    data class OnReorderModalVisible(val visible: Boolean) : CategoriesScreenEvent
    data class OnSortOrderModalVisible(val visible: Boolean) : CategoriesScreenEvent
    data class OnCategoryModalVisible(val categoryModalData: CategoryModalData?) :
        CategoriesScreenEvent
    data class OnSearchQueryUpdate(val queryString: String) : CategoriesScreenEvent
}

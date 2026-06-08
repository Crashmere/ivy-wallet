package com.ivy.domain.usecase.category

import com.ivy.data.api.CategorySortOrderStore
import javax.inject.Inject

class SetCategorySortOrderPreferenceUseCase @Inject internal constructor(
    private val categorySortOrderStore: CategorySortOrderStore,
) {
    operator fun invoke(sortOrder: Int) {
        categorySortOrderStore.categorySortOrder = sortOrder
    }
}

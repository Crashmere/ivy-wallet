package com.ivy.domain.usecase.category

import com.ivy.data.api.CategorySortOrderStore
import javax.inject.Inject

class GetCategorySortOrderPreferenceUseCase @Inject internal constructor(
    private val categorySortOrderStore: CategorySortOrderStore,
) {
    operator fun invoke(): Int {
        return categorySortOrderStore.categorySortOrder
    }
}

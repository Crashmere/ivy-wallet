package com.ivy.domain.usecase.category

import com.ivy.data.api.CategoryStore
import com.ivy.data.model.Category
import javax.inject.Inject

class SaveCategoryUseCase @Inject internal constructor(
    private val categoryStore: CategoryStore
) {
    suspend operator fun invoke(category: Category) {
        categoryStore.save(category)
    }
}

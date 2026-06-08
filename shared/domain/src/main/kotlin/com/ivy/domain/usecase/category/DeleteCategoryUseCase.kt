package com.ivy.domain.usecase.category

import com.ivy.data.api.CategoryStore
import com.ivy.data.model.CategoryId
import javax.inject.Inject

class DeleteCategoryUseCase @Inject internal constructor(
    private val categoryStore: CategoryStore
) {
    suspend operator fun invoke(categoryId: CategoryId) {
        categoryStore.deleteById(categoryId)
    }
}

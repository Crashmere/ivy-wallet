package com.ivy.domain.usecase.category

import com.ivy.data.api.CategoryStore
import com.ivy.data.model.Category
import javax.inject.Inject

class GetCategoriesUseCase @Inject internal constructor(
    private val categoryStore: CategoryStore
) {
    suspend operator fun invoke(): List<Category> {
        return categoryStore.findAll()
    }
}

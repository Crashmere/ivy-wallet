package com.ivy.domain.usecase.category

import com.ivy.data.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateCategoryUseCase @Inject internal constructor(
    private val saveCategoryUseCase: SaveCategoryUseCase,
) {
    suspend operator fun invoke(category: Category): Boolean {
        if (category.name.value.isBlank()) return false

        return try {
            withContext(Dispatchers.IO) {
                saveCategoryUseCase(category)
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}

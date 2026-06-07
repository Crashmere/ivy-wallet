package com.ivy.domain.usecase.category

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.model.Category
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(category: Category): Boolean {
        if (category.name.value.isBlank()) return false

        return try {
            withContext(dispatchers.io) {
                saveCategoryUseCase(category)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

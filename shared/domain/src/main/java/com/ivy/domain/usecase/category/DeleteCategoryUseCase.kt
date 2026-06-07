package com.ivy.domain.usecase.category

import com.ivy.data.model.CategoryId
import com.ivy.data.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: CategoryId) {
        categoryRepository.deleteById(categoryId)
    }
}

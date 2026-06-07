package com.ivy.domain.usecase.category

import com.ivy.data.model.Category
import com.ivy.data.repository.CategoryRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(): List<Category> {
        return categoryRepository.findAll()
    }
}

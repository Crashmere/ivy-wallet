package com.ivy.domain.usecase.category

import arrow.core.raise.either
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.legacy.CreateCategoryData
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.repository.CategoryRepository
import com.ivy.domain.util.nextOrderNum
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(data: CreateCategoryData): Category? {
        val name = data.name
        if (name.isBlank()) return null

        return try {
            withContext(dispatchers.io) {
                val newCategory = either {
                    Category(
                        name = NotBlankTrimmedString.from(name.trim()).bind(),
                        color = ColorInt(data.color),
                        icon = data.icon?.let(IconAsset::from)?.getOrNull(),
                        orderNum = categoryRepository.findMaxOrderNum().nextOrderNum(),
                        id = CategoryId(UUID.randomUUID()),
                    )
                }.getOrNull()

                if (newCategory != null) {
                    saveCategoryUseCase(newCategory)
                }
                newCategory
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

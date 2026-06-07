package com.ivy.domain.usecase.category

import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class SetCategorySortOrderPreferenceUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore,
) {
    operator fun invoke(sortOrder: Int) {
        appPreferences.categorySortOrder = sortOrder
    }
}

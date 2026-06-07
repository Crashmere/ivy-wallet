package com.ivy.domain.usecase.category

import com.ivy.domain.preferences.AppPreferences
import javax.inject.Inject

class SetCategorySortOrderPreferenceUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(sortOrder: Int) {
        appPreferences.categorySortOrder = sortOrder
    }
}

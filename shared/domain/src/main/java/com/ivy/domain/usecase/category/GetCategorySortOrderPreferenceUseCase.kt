package com.ivy.domain.usecase.category

import com.ivy.domain.preferences.AppPreferences
import javax.inject.Inject

class GetCategorySortOrderPreferenceUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Int {
        return appPreferences.categorySortOrder
    }
}

package com.ivy.domain.usecase.category

import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class GetCategorySortOrderPreferenceUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore,
) {
    operator fun invoke(): Int {
        return appPreferences.categorySortOrder
    }
}

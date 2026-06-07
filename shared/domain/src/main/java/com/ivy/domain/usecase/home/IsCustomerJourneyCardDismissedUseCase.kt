package com.ivy.domain.usecase.home

import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class IsCustomerJourneyCardDismissedUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore,
) {
    operator fun invoke(cardId: String): Boolean {
        return appPreferences.isCustomerJourneyCardDismissed(cardId)
    }
}

package com.ivy.domain.usecase.home

import com.ivy.domain.preferences.AppPreferences
import javax.inject.Inject

class IsCustomerJourneyCardDismissedUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(cardId: String): Boolean {
        return appPreferences.isCustomerJourneyCardDismissed(cardId)
    }
}

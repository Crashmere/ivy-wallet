package com.ivy.domain.usecase.home

import com.ivy.domain.preferences.AppPreferences
import javax.inject.Inject

class DismissCustomerJourneyCardUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(cardId: String) {
        appPreferences.dismissCustomerJourneyCard(cardId)
    }
}

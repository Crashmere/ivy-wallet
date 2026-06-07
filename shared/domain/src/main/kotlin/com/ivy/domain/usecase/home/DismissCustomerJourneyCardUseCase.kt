package com.ivy.domain.usecase.home

import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class DismissCustomerJourneyCardUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore,
) {
    operator fun invoke(cardId: String) {
        appPreferences.dismissCustomerJourneyCard(cardId)
    }
}

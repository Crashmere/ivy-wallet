package com.ivy.domain.usecase.home

import com.ivy.data.api.CustomerJourneyCardStore
import javax.inject.Inject

class IsCustomerJourneyCardDismissedUseCase @Inject internal constructor(
    private val customerJourneyCardStore: CustomerJourneyCardStore,
) {
    operator fun invoke(cardId: String): Boolean {
        return customerJourneyCardStore.isCustomerJourneyCardDismissed(cardId)
    }
}

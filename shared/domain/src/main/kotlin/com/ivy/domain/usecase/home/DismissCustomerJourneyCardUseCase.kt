package com.ivy.domain.usecase.home

import com.ivy.data.api.CustomerJourneyCardStore
import javax.inject.Inject

class DismissCustomerJourneyCardUseCase @Inject internal constructor(
    private val customerJourneyCardStore: CustomerJourneyCardStore,
) {
    operator fun invoke(cardId: String) {
        customerJourneyCardStore.dismissCustomerJourneyCard(cardId)
    }
}

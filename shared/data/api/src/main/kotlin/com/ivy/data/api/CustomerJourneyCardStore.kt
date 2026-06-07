package com.ivy.data.api

interface CustomerJourneyCardStore {
    fun isCustomerJourneyCardDismissed(cardId: String): Boolean

    fun dismissCustomerJourneyCard(cardId: String)
}

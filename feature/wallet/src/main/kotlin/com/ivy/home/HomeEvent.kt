package com.ivy.home

import com.ivy.home.customerjourney.CustomerJourneyCardModel
import com.ivy.ui.period.TimePeriod

internal sealed interface HomeEvent {
    data object BalanceClick : HomeEvent
    data object HiddenBalanceClick : HomeEvent
    data object HiddenIncomeClick : HomeEvent
    data class SetExpanded(val expanded: Boolean) : HomeEvent

    data class SetPeriod(val period: TimePeriod) : HomeEvent

    data class DismissCustomerJourneyCard(val card: CustomerJourneyCardModel) : HomeEvent

    data object SelectNextMonth : HomeEvent
    data object SelectPreviousMonth : HomeEvent
}

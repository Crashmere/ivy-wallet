package com.ivy.home

import com.ivy.home.customerjourney.CustomerJourneyCardModel
import com.ivy.ui.period.TimePeriod
import java.util.UUID

sealed interface HomeEvent {
    data class SetUpcomingExpanded(val expanded: Boolean) : HomeEvent
    data class SetOverdueExpanded(val expanded: Boolean) : HomeEvent

    data object BalanceClick : HomeEvent
    data object HiddenBalanceClick : HomeEvent
    data object HiddenIncomeClick : HomeEvent
    data class SetExpanded(val expanded: Boolean) : HomeEvent

    data object SwitchTheme : HomeEvent

    data class SetBuffer(val buffer: Double) : HomeEvent

    data class SetCurrency(val currency: String) : HomeEvent

    data class SetPeriod(val period: TimePeriod) : HomeEvent

    data class PayOrGetPlanned(val transactionId: UUID) : HomeEvent
    data class SkipPlanned(val transactionId: UUID) : HomeEvent
    data class SkipAllPlanned(val transactionIds: List<UUID>) : HomeEvent

    data class DismissCustomerJourneyCard(val card: CustomerJourneyCardModel) : HomeEvent

    data object SelectNextMonth : HomeEvent
    data object SelectPreviousMonth : HomeEvent
}

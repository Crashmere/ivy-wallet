package com.ivy.home.customerjourney

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

sealed interface CustomerJourneyAction {
    data object OpenAccountsTab : CustomerJourneyAction
    data object AddPlannedPayment : CustomerJourneyAction
    data object OpenExpensePieChart : CustomerJourneyAction
}

@Immutable
data class CustomerJourneyCardModel(
    val id: String,
    @Suppress("MaximumLineLength", "ParameterWrapping", "MaxLineLength", "ParameterListWrapping")
    val condition: suspend (transactionCount: Long, plannedPaymentsCount: Long) -> Boolean,
    val title: String,
    val description: String,
    val cta: String?,
    @DrawableRes val ctaIcon: Int,

    val hasDismiss: Boolean = true,

    val backgroundColorArgb: Int,
    val action: CustomerJourneyAction
)

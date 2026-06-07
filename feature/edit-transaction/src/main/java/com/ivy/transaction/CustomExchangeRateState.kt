package com.ivy.transaction


data class CustomExchangeRateState(
    val showCard: Boolean = false,
    val toCurrencyCode: String? = null,
    val fromCurrencyCode: String? = null,
    val exchangeRate: Double = 1.0,
    val convertedAmount: Double? = null
)

package com.ivy.data.model.legacy

data class ExchangeRate(
    val baseCurrency: String,
    val currency: String,
    val rate: Double,
)

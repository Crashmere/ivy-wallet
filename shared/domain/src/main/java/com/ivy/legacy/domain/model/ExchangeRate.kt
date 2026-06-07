package com.ivy.legacy.domain.model

import com.ivy.data.db.entity.ExchangeRateEntity

@Deprecated("Legacy data model. Will be deleted")
data class ExchangeRate(
    val baseCurrency: String,
    val currency: String,
    val rate: Double,
) {
    fun toEntity(): ExchangeRateEntity = ExchangeRateEntity(
        baseCurrency = baseCurrency,
        currency = currency,
        rate = rate
    )
}

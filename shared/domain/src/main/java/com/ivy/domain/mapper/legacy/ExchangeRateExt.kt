package com.ivy.domain.mapper.legacy

import com.ivy.data.db.entity.ExchangeRateEntity
import com.ivy.data.model.legacy.ExchangeRate

fun ExchangeRateEntity.toLegacyDomain(): ExchangeRate = ExchangeRate(
    baseCurrency = baseCurrency,
    currency = currency,
    rate = rate
)

fun ExchangeRate.toEntity(): ExchangeRateEntity = ExchangeRateEntity(
    baseCurrency = baseCurrency,
    currency = currency,
    rate = rate
)

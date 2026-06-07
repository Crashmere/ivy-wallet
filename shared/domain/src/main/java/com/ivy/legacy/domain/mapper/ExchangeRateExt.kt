package com.ivy.legacy.domain.mapper

import com.ivy.data.db.entity.ExchangeRateEntity
import com.ivy.legacy.datamodel.ExchangeRate

fun ExchangeRateEntity.toLegacyDomain(): ExchangeRate = ExchangeRate(
    baseCurrency = baseCurrency,
    currency = currency,
    rate = rate
)

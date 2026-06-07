package com.ivy.domain.mapper.legacy

import com.ivy.data.model.ExchangeRate
import com.ivy.data.model.legacy.ExchangeRate as LegacyExchangeRate

fun ExchangeRate.toLegacyDomain(): LegacyExchangeRate = LegacyExchangeRate(
    baseCurrency = baseCurrency.code,
    currency = currency.code,
    rate = rate.value
)

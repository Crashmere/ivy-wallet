package com.ivy.data.model.currency

import java.util.Currency
import java.util.Locale

fun getDefaultFIATCurrency(): Currency =
    runCatching { Currency.getInstance(Locale.getDefault()) }.getOrNull()
        ?: Currency.getInstance("USD")

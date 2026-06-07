package com.ivy.base.currency

import android.icu.util.Currency
import java.util.Locale

fun getDefaultFIATCurrency(): Currency =
    Currency.getInstance(Locale.getDefault()) ?: Currency.getInstance("USD")
        ?: Currency.getInstance("usd") ?: Currency.getAvailableCurrencies().firstOrNull()
        ?: Currency.getInstance("EUR")

package com.ivy.data.api

import com.ivy.data.model.primitive.AssetCode

interface CurrencyStore {
    suspend fun getBaseCurrency(): AssetCode

    suspend fun getBaseCurrencyCode(): String

    suspend fun setBaseCurrency(newCurrency: AssetCode)
}

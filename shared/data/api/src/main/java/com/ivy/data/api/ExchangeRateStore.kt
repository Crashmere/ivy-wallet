package com.ivy.data.api

import arrow.core.Either
import com.ivy.data.model.ExchangeRate
import com.ivy.data.model.primitive.AssetCode
import kotlinx.coroutines.flow.Flow

interface ExchangeRateStore {
    suspend fun fetchEurExchangeRates(): Either<String, List<ExchangeRate>>

    fun findAll(): Flow<List<ExchangeRate>>

    suspend fun findAllManuallyOverridden(): List<ExchangeRate>

    suspend fun findByBaseCurrencyAndCurrency(
        baseCurrency: AssetCode,
        currency: AssetCode,
    ): ExchangeRate?

    suspend fun save(value: ExchangeRate)

    suspend fun saveManyRates(values: List<ExchangeRate>)

    suspend fun deleteAll()

    suspend fun deleteByBaseCurrencyAndCurrency(
        baseCurrency: AssetCode,
        currency: AssetCode,
    )
}

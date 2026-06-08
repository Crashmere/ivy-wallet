package com.ivy.data.store

import arrow.core.Either
import arrow.core.raise.either
import com.ivy.data.api.ExchangeRateStore
import com.ivy.data.db.dao.read.ExchangeRatesDao
import com.ivy.data.db.dao.write.WriteExchangeRatesDao
import com.ivy.data.model.ExchangeRate
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.remote.RemoteExchangeRatesDataSource
import com.ivy.data.mapper.ExchangeRateMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultExchangeRateStore @Inject internal constructor(
    private val mapper: ExchangeRateMapper,
    private val exchangeRatesDao: ExchangeRatesDao,
    private val writeExchangeRatesDao: WriteExchangeRatesDao,
    private val remoteExchangeRatesDataSource: RemoteExchangeRatesDataSource,
) : ExchangeRateStore {
    override suspend fun fetchEurExchangeRates(): Either<String, List<ExchangeRate>> = either {
        withContext(Dispatchers.IO) {
            val response = remoteExchangeRatesDataSource.fetchEurExchangeRates().bind()
            with(mapper) { response.toDomain().bind() }
        }
    }

    override fun findAll(): Flow<List<ExchangeRate>> =
        exchangeRatesDao.findAll().map { entities ->
            entities.mapNotNull {
                with(mapper) { it.toDomain().getOrNull() }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun findAllManuallyOverridden(): List<ExchangeRate> =
        withContext(Dispatchers.IO) {
            exchangeRatesDao.findAllManuallyOverridden()
                .mapNotNull {
                    with(mapper) { it.toDomain().getOrNull() }
                }
        }

    override suspend fun findByBaseCurrencyAndCurrency(
        baseCurrency: AssetCode,
        currency: AssetCode
    ): ExchangeRate? = withContext(Dispatchers.IO) {
        exchangeRatesDao.findByBaseCurrencyAndCurrency(
            baseCurrency = baseCurrency.code,
            currency = currency.code
        )?.let {
            with(mapper) { it.toDomain().getOrNull() }
        }
    }

    override suspend fun save(value: ExchangeRate) {
        withContext(Dispatchers.IO) {
            writeExchangeRatesDao.save(with(mapper) { value.toEntity() })
        }
    }

    override suspend fun saveManyRates(values: List<ExchangeRate>) {
        withContext(Dispatchers.IO) {
            writeExchangeRatesDao.saveMany(
                values.map {
                    with(mapper) { it.toEntity() }
                },
            )
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            writeExchangeRatesDao.deleteAll()
        }
    }

    override suspend fun deleteByBaseCurrencyAndCurrency(
        baseCurrency: AssetCode,
        currency: AssetCode
    ): Unit = withContext(Dispatchers.IO) {
        writeExchangeRatesDao.deleteByBaseCurrencyAndCurrency(
            baseCurrency = baseCurrency.code,
            currency = currency.code
        )
    }
}

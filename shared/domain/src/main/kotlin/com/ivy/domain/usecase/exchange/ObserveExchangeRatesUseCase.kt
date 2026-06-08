package com.ivy.domain.usecase.exchange

import com.ivy.data.api.ExchangeRateStore
import com.ivy.data.model.ExchangeRate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveExchangeRatesUseCase @Inject internal constructor(
    private val exchangeRateStore: ExchangeRateStore
) {
    operator fun invoke(): Flow<List<ExchangeRate>> {
        return exchangeRateStore.findAll()
    }
}

package com.ivy.domain.usecase.exchange

import com.ivy.data.api.ExchangeRateStore
import com.ivy.data.model.ExchangeRate
import javax.inject.Inject

class SaveExchangeRateUseCase @Inject internal constructor(
    private val exchangeRateStore: ExchangeRateStore
) {
    suspend operator fun invoke(exchangeRate: ExchangeRate) {
        exchangeRateStore.save(exchangeRate)
    }
}

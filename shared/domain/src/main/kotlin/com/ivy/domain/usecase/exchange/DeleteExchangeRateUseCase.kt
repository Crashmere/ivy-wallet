package com.ivy.domain.usecase.exchange

import com.ivy.data.api.ExchangeRateStore
import com.ivy.data.model.primitive.AssetCode
import javax.inject.Inject

class DeleteExchangeRateUseCase @Inject constructor(
    private val exchangeRateStore: ExchangeRateStore
) {
    suspend operator fun invoke(
        baseCurrency: AssetCode,
        currency: AssetCode
    ) {
        exchangeRateStore.deleteByBaseCurrencyAndCurrency(
            baseCurrency = baseCurrency,
            currency = currency
        )
    }
}

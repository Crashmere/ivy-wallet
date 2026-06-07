package com.ivy.domain.usecase.exchange

import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.repository.ExchangeRatesRepository
import javax.inject.Inject

class DeleteExchangeRateUseCase @Inject constructor(
    private val repository: ExchangeRatesRepository
) {
    suspend operator fun invoke(
        baseCurrency: AssetCode,
        currency: AssetCode
    ) {
        repository.deleteByBaseCurrencyAndCurrency(
            baseCurrency = baseCurrency,
            currency = currency
        )
    }
}

package com.ivy.domain.usecase.exchange

import com.ivy.data.model.ExchangeRate
import com.ivy.data.repository.ExchangeRatesRepository
import javax.inject.Inject

class SaveExchangeRateUseCase @Inject constructor(
    private val repository: ExchangeRatesRepository
) {
    suspend operator fun invoke(exchangeRate: ExchangeRate) {
        repository.save(exchangeRate)
    }
}

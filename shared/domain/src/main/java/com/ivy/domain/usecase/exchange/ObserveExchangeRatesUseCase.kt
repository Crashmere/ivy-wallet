package com.ivy.domain.usecase.exchange

import com.ivy.data.model.ExchangeRate
import com.ivy.data.repository.ExchangeRatesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveExchangeRatesUseCase @Inject constructor(
    private val repository: ExchangeRatesRepository
) {
    operator fun invoke(): Flow<List<ExchangeRate>> {
        return repository.findAll()
    }
}

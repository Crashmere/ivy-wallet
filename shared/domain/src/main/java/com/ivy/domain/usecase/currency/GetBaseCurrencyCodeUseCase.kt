package com.ivy.domain.usecase.currency

import com.ivy.data.repository.CurrencyRepository
import javax.inject.Inject

class GetBaseCurrencyCodeUseCase @Inject constructor(
    private val currencyRepository: CurrencyRepository
) {
    suspend operator fun invoke(): String {
        return currencyRepository.getBaseCurrencyCode()
    }
}

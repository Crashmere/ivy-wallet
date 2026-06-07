package com.ivy.domain.usecase.currency

import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.repository.CurrencyRepository
import javax.inject.Inject

class GetBaseCurrencyUseCase @Inject constructor(
    private val currencyRepository: CurrencyRepository
) {
    suspend operator fun invoke(): AssetCode {
        return currencyRepository.getBaseCurrency()
    }
}

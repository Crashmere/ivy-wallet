package com.ivy.domain.usecase.currency

import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.repository.CurrencyRepository
import javax.inject.Inject

class SetBaseCurrencyUseCase @Inject constructor(
    private val currencyRepository: CurrencyRepository
) {
    suspend operator fun invoke(newCurrency: AssetCode) {
        currencyRepository.setBaseCurrency(newCurrency)
    }
}

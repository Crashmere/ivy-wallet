package com.ivy.domain.usecase.currency

import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.api.CurrencyStore
import javax.inject.Inject

class GetBaseCurrencyUseCase @Inject constructor(
    private val currencyStore: CurrencyStore
) {
    suspend operator fun invoke(): AssetCode {
        return currencyStore.getBaseCurrency()
    }
}

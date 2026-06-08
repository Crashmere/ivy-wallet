package com.ivy.domain.usecase.exchange

import com.ivy.data.api.ExchangeRateStore
import com.ivy.data.model.primitive.AssetCode
import javax.inject.Inject

internal class LegacyExchangeRatesUseCase @Inject constructor(
    private val exchangeRateStore: ExchangeRateStore
) {
    suspend fun convertAmount(
        baseCurrency: String,
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Double {
        if (fromCurrency == toCurrency) return amount

        val amountBaseCurrency =
            amount / exchangeRate(baseCurrency = baseCurrency, currency = fromCurrency)
        return amountBaseCurrency * exchangeRate(baseCurrency = baseCurrency, currency = toCurrency)
    }

    /**
     * base = BGN, currency = EUR => rate = 0.51
     */
    private suspend fun exchangeRate(
        baseCurrency: String,
        currency: String
    ): Double {
        val base = AssetCode.from(baseCurrency).getOrNull() ?: return 1.0
        val target = AssetCode.from(currency).getOrNull() ?: return 1.0
        val rate = exchangeRateStore.findByBaseCurrencyAndCurrency(
            baseCurrency = base,
            currency = target
        )?.rate?.value ?: return 1.0
        if (rate <= 0) {
            return 1.0
        }
        return rate
    }
}

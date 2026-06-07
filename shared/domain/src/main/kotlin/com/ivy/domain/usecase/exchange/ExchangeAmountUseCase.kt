package com.ivy.domain.usecase.exchange

import arrow.core.Option
import com.ivy.data.api.ExchangeRateStore
import com.ivy.data.model.primitive.AssetCode
import com.ivy.domain.exchange.ExchangeData
import com.ivy.domain.exchange.exchange
import java.math.BigDecimal
import javax.inject.Inject

class ExchangeAmountUseCase @Inject constructor(
    private val exchangeRateStore: ExchangeRateStore,
) {
    suspend operator fun invoke(
        data: ExchangeData,
        amount: BigDecimal
    ): Option<BigDecimal> {
        return exchange(
            data = data,
            amount = amount,
            getExchangeRate = { baseCurrency, toCurrency ->
                val base = AssetCode.from(baseCurrency).getOrNull()
                val target = AssetCode.from(toCurrency).getOrNull()
                if (base == null || target == null) {
                    null
                } else {
                    exchangeRateStore.findByBaseCurrencyAndCurrency(base, target)
                        ?.rate
                        ?.value
                        ?.toBigDecimal()
                }
            }
        )
    }
}

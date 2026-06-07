package com.ivy.domain.usecase.exchange

import arrow.core.Option
import com.ivy.data.db.dao.read.ExchangeRatesDao
import com.ivy.domain.mapper.legacy.toLegacyDomain
import com.ivy.domain.exchange.ExchangeData
import com.ivy.domain.exchange.exchange
import java.math.BigDecimal
import javax.inject.Inject

class ExchangeAmountUseCase @Inject constructor(
    private val exchangeRatesDao: ExchangeRatesDao,
) {
    suspend operator fun invoke(
        data: ExchangeData,
        amount: BigDecimal
    ): Option<BigDecimal> {
        return exchange(
            data = data,
            amount = amount,
            getExchangeRate = { baseCurrency, toCurrency ->
                exchangeRatesDao.findByBaseCurrencyAndCurrency(baseCurrency, toCurrency)
                    ?.toLegacyDomain()
            }
        )
    }
}

package com.ivy.domain.usecase.exchange

import com.ivy.data.model.Transaction
import com.ivy.data.model.getFromValue
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.domain.transaction.transactionCurrency
import java.math.BigDecimal
import javax.inject.Inject

class ExchangeTransactionAmountUseCase @Inject constructor(
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
) {
    suspend operator fun invoke(
        transaction: Transaction,
        accounts: List<LegacyAccount>,
        baseCurrency: String,
        toCurrency: String = baseCurrency,
    ): BigDecimal {
        return exchangeAmountUseCase(
            amount = transaction.getFromValue().amount.value.toBigDecimal(),
            baseCurrency = baseCurrency,
            fromCurrency = transactionCurrency(
                transaction = transaction,
                accounts = accounts,
                baseCurrency = baseCurrency
            ).getOrNull(),
            toCurrency = toCurrency
        ).getOrNull() ?: BigDecimal.ZERO
    }
}

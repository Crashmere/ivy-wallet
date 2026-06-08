package com.ivy.domain.transaction.legacy

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.legacy.legacyAccountCurrency
import com.ivy.domain.exchange.ExchangeData
import com.ivy.domain.exchange.ExchangeEffect
import com.ivy.domain.exchange.ExchangeTransactionArgument
import java.math.BigDecimal

internal suspend fun exchangeInBaseCurrency(
    transaction: LegacyTransaction,
    arg: ExchangeTransactionArgument
): BigDecimal {
    val fromCurrency = arg.getAccount(transaction.accountId)?.let {
        legacyAccountCurrency(it, arg.baseCurrency)
    }.toOption()

    return exchangeInCurrency(
        transaction = transaction,
        baseCurrency = arg.baseCurrency,
        transactionCurrency = fromCurrency,
        toCurrency = arg.baseCurrency,
        exchange = arg.exchange
    )
}

internal suspend fun exchangeInCurrency(
    transaction: LegacyTransaction,
    baseCurrency: String,
    transactionCurrency: Option<String>,
    toCurrency: String,
    exchange: ExchangeEffect
): BigDecimal {
    return exchange(
        ExchangeData(
            baseCurrency = baseCurrency,
            fromCurrency = transactionCurrency,
            toCurrency = toCurrency
        ),
        transaction.amount
    ).getOrNull() ?: BigDecimal.ZERO
}

internal object LegacyExchangeTransactions {
    suspend fun exchangeInBaseCurrency(
        transaction: LegacyTransaction,
        baseCurrency: String,
        accounts: List<LegacyAccount>,
        exchange: ExchangeEffect
    ): BigDecimal = exchangeInCurrency(
        transaction = transaction,
        baseCurrency = baseCurrency,
        accounts = accounts,
        toCurrency = baseCurrency,
        exchange = exchange
    )

    suspend fun exchangeInCurrency(
        transaction: LegacyTransaction,
        baseCurrency: String,
        accounts: List<LegacyAccount>,
        toCurrency: String,
        exchange: ExchangeEffect
    ): BigDecimal {
        return exchange(
            ExchangeData(
                baseCurrency = baseCurrency,
                fromCurrency = LegacyTransactionFunctions.transactionCurrency(transaction, accounts, baseCurrency),
                toCurrency = toCurrency
            ),
            transaction.amount
        ).getOrNull() ?: BigDecimal.ZERO
    }
}

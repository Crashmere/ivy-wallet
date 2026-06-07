package com.ivy.domain.transaction.legacy

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.legacy.Transaction
import com.ivy.domain.account.legacy.legacyAccountCurrency
import com.ivy.domain.exchange.ExchangeData
import com.ivy.domain.exchange.ExchangeEffect
import com.ivy.domain.exchange.ExchangeTransactionArgument
import java.math.BigDecimal

suspend fun exchangeInBaseCurrency(
    transaction: Transaction,
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

suspend fun exchangeInCurrency(
    transaction: Transaction,
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

object LegacyExchangeTransactions {
    suspend fun exchangeInBaseCurrency(
        transaction: Transaction,
        baseCurrency: String,
        accounts: List<Account>,
        exchange: ExchangeEffect
    ): BigDecimal = exchangeInCurrency(
        transaction = transaction,
        baseCurrency = baseCurrency,
        accounts = accounts,
        toCurrency = baseCurrency,
        exchange = exchange
    )

    suspend fun exchangeInCurrency(
        transaction: Transaction,
        baseCurrency: String,
        accounts: List<Account>,
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

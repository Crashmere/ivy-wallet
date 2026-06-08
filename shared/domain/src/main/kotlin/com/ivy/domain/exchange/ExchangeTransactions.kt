package com.ivy.domain.exchange

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.model.Transaction
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.domain.account.legacy.legacyAccountCurrency
import com.ivy.domain.transaction.getAccountId
import com.ivy.domain.transaction.getValue
import com.ivy.domain.transaction.transactionCurrency
import java.math.BigDecimal
import java.util.UUID

typealias ExchangeEffect = suspend (ExchangeData, BigDecimal) -> Option<BigDecimal>

data class ExchangeTransactionArgument(
    val baseCurrency: String,
    val getAccount: suspend (accountId: UUID) -> LegacyAccount?,
    val exchange: ExchangeEffect
)
suspend fun exchangeInBaseCurrency(
    transaction: Transaction,
    arg: ExchangeTransactionArgument
): BigDecimal {
    val fromCurrency = arg.getAccount(transaction.getAccountId())?.let {
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
suspend fun exchangeInBaseCurrency(
    transaction: Transaction,
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
    transaction: Transaction,
    baseCurrency: String,
    accounts: List<LegacyAccount>,
    toCurrency: String,
    exchange: ExchangeEffect
): BigDecimal {
    return exchange(
        ExchangeData(
            baseCurrency = baseCurrency,
            fromCurrency = transactionCurrency(transaction, accounts, baseCurrency),
            toCurrency = toCurrency
        ),
        transaction.getValue()
    ).getOrNull() ?: BigDecimal.ZERO
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
        transaction.getValue()
    ).getOrNull() ?: BigDecimal.ZERO
}

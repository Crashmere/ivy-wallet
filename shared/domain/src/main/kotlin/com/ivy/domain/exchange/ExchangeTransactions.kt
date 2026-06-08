package com.ivy.domain.exchange

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.model.Account
import com.ivy.data.model.Transaction
import com.ivy.domain.transaction.getAccountId
import com.ivy.domain.transaction.getValue
import com.ivy.domain.transaction.transactionCurrency
import java.math.BigDecimal
import java.util.UUID

internal typealias ExchangeEffect = suspend (ExchangeData, BigDecimal) -> Option<BigDecimal>

internal data class ExchangeTransactionArgument(
    val baseCurrency: String,
    val getAccount: suspend (accountId: UUID) -> Account?,
    val exchange: ExchangeEffect
)

internal suspend fun exchangeInBaseCurrency(
    transaction: Transaction,
    arg: ExchangeTransactionArgument
): BigDecimal {
    val fromCurrency = arg.getAccount(transaction.getAccountId())?.let {
        it.asset.code
    }.toOption()

    return exchangeInCurrency(
        transaction = transaction,
        baseCurrency = arg.baseCurrency,
        transactionCurrency = fromCurrency,
        toCurrency = arg.baseCurrency,
        exchange = arg.exchange
    )
}
internal suspend fun exchangeInBaseCurrency(
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
internal suspend fun exchangeInCurrency(
    transaction: Transaction,
    baseCurrency: String,
    accounts: List<Account>,
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

internal suspend fun exchangeInCurrency(
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

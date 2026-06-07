package com.ivy.domain.exchange

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.model.Transaction
import com.ivy.data.model.legacy.Account
import com.ivy.domain.account.accountCurrency
import com.ivy.domain.transaction.legacy.LegacyTrnFunctions
import com.ivy.domain.transaction.getAccountId
import com.ivy.domain.transaction.getValue
import com.ivy.domain.transaction.transactionCurrency
import java.math.BigDecimal
import java.util.UUID

typealias ExchangeEffect = suspend (ExchangeData, BigDecimal) -> Option<BigDecimal>

data class ExchangeTransactionArgument(
    val baseCurrency: String,
    val getAccount: suspend (accountId: UUID) -> Account?,
    val exchange: ExchangeEffect
)
suspend fun exchangeInBaseCurrency(
    transaction: Transaction,
    arg: ExchangeTransactionArgument
): BigDecimal {
    val fromCurrency = arg.getAccount(transaction.getAccountId())?.let {
        accountCurrency(it, arg.baseCurrency)
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
    transaction: com.ivy.data.model.legacy.Transaction,
    arg: ExchangeTransactionArgument
): BigDecimal {
    val fromCurrency = arg.getAccount(transaction.accountId)?.let {
        accountCurrency(it, arg.baseCurrency)
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

suspend fun exchangeInCurrency(
    transaction: com.ivy.data.model.legacy.Transaction,
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
        transaction: com.ivy.data.model.legacy.Transaction,
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
        transaction: com.ivy.data.model.legacy.Transaction,
        baseCurrency: String,
        accounts: List<Account>,
        toCurrency: String,
        exchange: ExchangeEffect
    ): BigDecimal {
        return exchange(
            ExchangeData(
                baseCurrency = baseCurrency,
                fromCurrency = LegacyTrnFunctions.transactionCurrency(transaction, accounts, baseCurrency),
                toCurrency = toCurrency
            ),
            transaction.amount
        ).getOrNull() ?: BigDecimal.ZERO
    }
}

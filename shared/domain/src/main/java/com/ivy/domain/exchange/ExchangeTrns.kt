package com.ivy.domain.exchange

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.model.Transaction
import com.ivy.data.model.legacy.Account
import com.ivy.domain.account.accountCurrency
import com.ivy.domain.transaction.legacy.LegacyTrnFunctions
import com.ivy.domain.transaction.legacy.getAccountId
import com.ivy.domain.transaction.legacy.getValue
import com.ivy.domain.transaction.legacy.trnCurrency
import java.math.BigDecimal
import java.util.UUID

typealias ExchangeEffect = suspend (ExchangeData, BigDecimal) -> Option<BigDecimal>

data class ExchangeTrnArgument(
    val baseCurrency: String,
    val getAccount: suspend (accountId: UUID) -> Account?,
    val exchange: ExchangeEffect
)
suspend fun exchangeInBaseCurrency(
    transaction: Transaction,
    arg: ExchangeTrnArgument
): BigDecimal {
    val fromCurrency = arg.getAccount(transaction.getAccountId())?.let {
        accountCurrency(it, arg.baseCurrency)
    }.toOption()

    return exchangeInCurrency(
        transaction = transaction,
        baseCurrency = arg.baseCurrency,
        trnCurrency = fromCurrency,
        toCurrency = arg.baseCurrency,
        exchange = arg.exchange
    )
}
suspend fun exchangeInBaseCurrency(
    transaction: com.ivy.base.model.legacy.Transaction,
    arg: ExchangeTrnArgument
): BigDecimal {
    val fromCurrency = arg.getAccount(transaction.accountId)?.let {
        accountCurrency(it, arg.baseCurrency)
    }.toOption()

    return exchangeInCurrency(
        transaction = transaction,
        baseCurrency = arg.baseCurrency,
        trnCurrency = fromCurrency,
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
            fromCurrency = trnCurrency(transaction, accounts, baseCurrency),
            toCurrency = toCurrency
        ),
        transaction.getValue()
    ).getOrNull() ?: BigDecimal.ZERO
}

suspend fun exchangeInCurrency(
    transaction: Transaction,
    baseCurrency: String,
    trnCurrency: Option<String>,
    toCurrency: String,
    exchange: ExchangeEffect
): BigDecimal {
    return exchange(
        ExchangeData(
            baseCurrency = baseCurrency,
            fromCurrency = trnCurrency,
            toCurrency = toCurrency
        ),
        transaction.getValue()
    ).getOrNull() ?: BigDecimal.ZERO
}

suspend fun exchangeInCurrency(
    transaction: com.ivy.base.model.legacy.Transaction,
    baseCurrency: String,
    trnCurrency: Option<String>,
    toCurrency: String,
    exchange: ExchangeEffect
): BigDecimal {
    return exchange(
        ExchangeData(
            baseCurrency = baseCurrency,
            fromCurrency = trnCurrency,
            toCurrency = toCurrency
        ),
        transaction.amount
    ).getOrNull() ?: BigDecimal.ZERO
}

object LegacyExchangeTrns {
    suspend fun exchangeInBaseCurrency(
        transaction: com.ivy.base.model.legacy.Transaction,
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
        transaction: com.ivy.base.model.legacy.Transaction,
        baseCurrency: String,
        accounts: List<Account>,
        toCurrency: String,
        exchange: ExchangeEffect
    ): BigDecimal {
        return exchange(
            ExchangeData(
                baseCurrency = baseCurrency,
                fromCurrency = LegacyTrnFunctions.trnCurrency(transaction, accounts, baseCurrency),
                toCurrency = toCurrency
            ),
            transaction.amount
        ).getOrNull() ?: BigDecimal.ZERO
    }
}

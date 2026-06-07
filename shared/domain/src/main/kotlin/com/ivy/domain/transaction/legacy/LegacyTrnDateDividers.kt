package com.ivy.domain.transaction.legacy

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.model.legacy.TransactionHistoryItem
import com.ivy.data.model.legacy.TransactionHistoryDateDivider
import com.ivy.data.api.AccountStore
import com.ivy.data.model.AccountId
import com.ivy.data.model.legacy.Account
import com.ivy.domain.mapper.legacy.toLegacyDomain
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import com.ivy.domain.exchange.ExchangeData
import com.ivy.domain.exchange.ExchangeTransactionArgument
import com.ivy.domain.exchange.exchangeInBaseCurrency
import com.ivy.domain.time.toLocalDateInSystemZone
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

private fun LocalDateTime.toEpochSeconds() = toEpochSecond(ZoneOffset.UTC)

object LegacyTrnDateDividers {
    suspend fun List<com.ivy.data.model.legacy.Transaction>.withDateDividers(
        exchangeRatesUseCase: LegacyExchangeRatesUseCase,
        baseCurrencyCode: String,
        accountStore: AccountStore,
    ): List<TransactionHistoryItem> {
        return transactionsWithDateDividers(
            transactions = this,
            baseCurrencyCode = baseCurrencyCode,
            getAccount = { accountId -> accountStore.findById(AccountId(accountId))?.toLegacyDomain() },
            exchange = { data, amount ->
                exchangeRatesUseCase.convertAmount(
                    baseCurrency = data.baseCurrency,
                    fromCurrency = data.fromCurrency.getOrNull() ?: "",
                    toCurrency = data.toCurrency,
                    amount = amount.toDouble()
                ).toBigDecimal().toOption()
            },
        )
    }

    suspend fun transactionsWithDateDividers(
        transactions: List<com.ivy.data.model.legacy.Transaction>,
        baseCurrencyCode: String,
        getAccount: suspend (accountId: UUID) -> Account?,
        exchange: suspend (ExchangeData, BigDecimal) -> Option<BigDecimal>
    ): List<TransactionHistoryItem> {
        if (transactions.isEmpty()) return emptyList()

        return transactions
            .groupBy { it.dateTime?.toLocalDateInSystemZone() }
            .filterKeys { it != null }
            .toSortedMap { date1, date2 ->
                if (date1 == null || date2 == null) return@toSortedMap 0 // this case shouldn't happen
                (
                        date2.atStartOfDay().toEpochSeconds() - date1.atStartOfDay()
                            .toEpochSeconds()
                        ).toInt()
            }
            .flatMap { (date, transactionsForDate) ->
                val arg = ExchangeTransactionArgument(
                    baseCurrency = baseCurrencyCode,
                    getAccount = getAccount,
                    exchange = exchange
                )

                listOf<TransactionHistoryItem>(
                    TransactionHistoryDateDivider(
                        date = date!!,
                        income = LegacyFoldTransactions.sumTrns(
                            LegacyTrnFunctions.incomes(transactionsForDate),
                            ::exchangeInBaseCurrency,
                            arg
                        ).toDouble(),
                        expenses = LegacyFoldTransactions.sumTrns(
                            LegacyTrnFunctions.expenses(transactionsForDate),
                            ::exchangeInBaseCurrency,
                            arg
                        ).toDouble()
                    ),
                ).plus(transactionsForDate)
            }
    }
}

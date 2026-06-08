package com.ivy.domain.transaction.legacy
import com.ivy.data.model.legacy.LegacyTransaction

import arrow.core.Option
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.TransactionHistoryDateDivider
import com.ivy.data.api.AccountStore
import com.ivy.data.model.AccountId
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.domain.mapper.legacy.toLegacyAccount
import com.ivy.domain.exchange.ExchangeData
import com.ivy.domain.exchange.ExchangeTransactionArgument
import com.ivy.domain.time.toLocalDateInSystemZone
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

private fun LocalDateTime.toEpochSeconds() = toEpochSecond(ZoneOffset.UTC)

internal object LegacyTransactionDateDividers {
    suspend fun List<LegacyTransaction>.withDateDividers(
        exchangeAmountUseCase: ExchangeAmountUseCase,
        baseCurrencyCode: String,
        accountStore: AccountStore,
    ): List<TransactionHistoryItem> {
        return transactionsWithDateDividers(
            transactions = this,
            baseCurrencyCode = baseCurrencyCode,
            getAccount = { accountId -> accountStore.findById(AccountId(accountId))?.toLegacyAccount() },
            exchange = exchangeAmountUseCase::invoke,
        )
    }

    suspend fun transactionsWithDateDividers(
        transactions: List<LegacyTransaction>,
        baseCurrencyCode: String,
        getAccount: suspend (accountId: UUID) -> LegacyAccount?,
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
                        income = LegacyFoldTransactions.sumTransactions(
                            LegacyTransactionFunctions.incomes(transactionsForDate),
                            ::exchangeInBaseCurrency,
                            arg
                        ).toDouble(),
                        expenses = LegacyFoldTransactions.sumTransactions(
                            LegacyTransactionFunctions.expenses(transactionsForDate),
                            ::exchangeInBaseCurrency,
                            arg
                        ).toDouble()
                    ),
                ).plus(transactionsForDate)
            }
    }
}

package com.ivy.domain.transaction

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.api.AccountStore
import com.ivy.data.api.TagStore
import com.ivy.data.model.AccountId
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.data.model.Transaction
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.legacy.TransactionHistoryDateDivider
import com.ivy.data.model.legacy.TransactionHistoryItem
import com.ivy.domain.exchange.ExchangeData
import com.ivy.domain.exchange.ExchangeTransactionArgument
import com.ivy.domain.exchange.exchangeInBaseCurrency
import com.ivy.domain.mapper.legacy.toImmutableLegacyTags
import com.ivy.domain.mapper.legacy.toLegacy
import com.ivy.domain.mapper.legacy.toLegacyDomain
import com.ivy.domain.time.convertToLocal
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

private fun LocalDateTime.toEpochSeconds() = toEpochSecond(ZoneOffset.UTC)

suspend fun List<Transaction>.withDateDividers(
    exchangeRatesLogic: LegacyExchangeRatesUseCase,
    baseCurrencyCode: String,
    tagStore: TagStore,
    accountStore: AccountStore,
): List<TransactionHistoryItem> {
    return transactionsWithDateDividers(
        transactions = this,
        baseCurrencyCode = baseCurrencyCode,
        getAccount = { accountId -> accountStore.findById(AccountId(accountId))?.toLegacyDomain() },
        getTags = { tagsIds -> tagStore.findByIds(tagsIds) },
        exchange = { data, amount ->
            exchangeRatesLogic.convertAmount(
                baseCurrency = data.baseCurrency,
                fromCurrency = data.fromCurrency.getOrNull() ?: "",
                toCurrency = data.toCurrency,
                amount = amount.toDouble()
            ).toBigDecimal().toOption()
        }
    )
}

suspend fun transactionsWithDateDividers(
    transactions: List<Transaction>,
    baseCurrencyCode: String,
    getAccount: suspend (accountId: UUID) -> Account?,
    exchange: suspend (ExchangeData, BigDecimal) -> Option<BigDecimal>,
    getTags: suspend (tagIds: List<TagId>) -> List<Tag> = { emptyList() },
): List<TransactionHistoryItem> {
    if (transactions.isEmpty()) return emptyList()
    return transactions
        .groupBy { it.time.convertToLocal().toLocalDate() }
        .filterKeys { it != null }
        .toSortedMap { date1, date2 ->
            if (date1 == null || date2 == null) return@toSortedMap 0 // this case shouldn't happen
            (date2.atStartOfDay().toEpochSeconds() - date1.atStartOfDay().toEpochSeconds()).toInt()
        }
        .flatMap { (date, transactionsForDate) ->
            val arg = ExchangeTransactionArgument(
                baseCurrency = baseCurrencyCode,
                getAccount = getAccount,
                exchange = exchange
            )

            // Required to be interoperable with [TransactionHistoryItem]
            val legacyTransactionsForDate = transactionsForDate.map {
                it.toLegacy(tags = getTags(it.tags).toImmutableLegacyTags())
            }
            listOf<TransactionHistoryItem>(
                TransactionHistoryDateDivider(
                    date = date!!,
                    income = sumTrns(
                        incomes(transactionsForDate),
                        ::exchangeInBaseCurrency,
                        arg
                    ).toDouble(),
                    expenses = sumTrns(
                        expenses(transactionsForDate),
                        ::exchangeInBaseCurrency,
                        arg
                    ).toDouble()
                ),
            ).plus(legacyTransactionsForDate)
        }
}

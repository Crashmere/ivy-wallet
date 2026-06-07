package com.ivy.legacy.domain.pure.transaction

import arrow.core.Option
import arrow.core.toOption
import com.ivy.base.model.legacy.TransactionHistoryItem
import com.ivy.base.time.TimeConverter
import com.ivy.legacy.domain.time.convertToLocal
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.data.model.Transaction
import com.ivy.data.repository.AccountRepository
import com.ivy.data.repository.TagRepository
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.data.model.legacy.Account
import com.ivy.legacy.domain.mapper.toImmutableLegacyTags
import com.ivy.legacy.domain.mapper.toLegacyDomain
import com.ivy.base.time.toEpochSeconds
import com.ivy.legacy.domain.data.TransactionHistoryDateDivider
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import com.ivy.legacy.domain.pure.exchange.ExchangeData
import com.ivy.legacy.domain.pure.exchange.ExchangeTrnArgument
import com.ivy.legacy.domain.pure.exchange.exchangeInBaseCurrency
import com.ivy.legacy.domain.pure.transaction.LegacyFoldTransactions
import com.ivy.legacy.domain.pure.transaction.LegacyTrnFunctions
import com.ivy.legacy.domain.pure.transaction.expenses
import com.ivy.legacy.domain.pure.transaction.incomes
import com.ivy.legacy.domain.pure.transaction.sumTrns
import java.math.BigDecimal
import java.util.UUID

suspend fun List<Transaction>.withDateDividers(
    exchangeRatesLogic: LegacyExchangeRatesUseCase,
    baseCurrencyCode: String,
    accountDao: AccountDao,
    tagRepository: TagRepository,
    accountRepository: AccountRepository,
): List<TransactionHistoryItem> {
    return transactionsWithDateDividers(
        transactions = this,
        baseCurrencyCode = baseCurrencyCode,
        getAccount = { accountId -> accountDao.findById(accountId)?.toLegacyDomain() },
        getTags = { tagsIds -> tagRepository.findByIds(tagsIds) },
        accountRepository = accountRepository,
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
    accountRepository: AccountRepository,
    getAccount: suspend (accountId: UUID) -> Account?,
    exchange: suspend (ExchangeData, BigDecimal) -> Option<BigDecimal>,
    getTags: suspend (tagIds: List<TagId>) -> List<Tag> = { emptyList() },
): List<TransactionHistoryItem> {
    if (transactions.isEmpty()) return emptyList()
    val transactionsMapper = TransactionMapper(accountRepository)
    return transactions
        .groupBy { it.time.convertToLocal().toLocalDate() }
        .filterKeys { it != null }
        .toSortedMap { date1, date2 ->
            if (date1 == null || date2 == null) return@toSortedMap 0 // this case shouldn't happen
            (date2.atStartOfDay().toEpochSeconds() - date1.atStartOfDay().toEpochSeconds()).toInt()
        }
        .flatMap { (date, transactionsForDate) ->
            val arg = ExchangeTrnArgument(
                baseCurrency = baseCurrencyCode,
                getAccount = getAccount,
                exchange = exchange
            )

            // Required to be interoperable with [TransactionHistoryItem]
            val legacyTransactionsForDate = with(transactionsMapper) {
                transactionsForDate.map {
                    it.toEntity()
                        .toLegacyDomain(tags = getTags(it.tags).toImmutableLegacyTags())
                }
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

object LegacyTrnDateDividers {
        suspend fun List<com.ivy.base.model.legacy.Transaction>.withDateDividers(
        exchangeRatesLogic: LegacyExchangeRatesUseCase,
        baseCurrencyCode: String,
        accountDao: AccountDao,
        timeConverter: TimeConverter,
    ): List<TransactionHistoryItem> {
        return transactionsWithDateDividers(
            transactions = this,
            baseCurrencyCode = baseCurrencyCode,
            getAccount = { accountId -> accountDao.findById(accountId)?.toLegacyDomain() },
            exchange = { data, amount ->
                exchangeRatesLogic.convertAmount(
                    baseCurrency = data.baseCurrency,
                    fromCurrency = data.fromCurrency.getOrNull() ?: "",
                    toCurrency = data.toCurrency,
                    amount = amount.toDouble()
                ).toBigDecimal().toOption()
            },
            timeConverter = timeConverter,
        )
    }
    suspend fun transactionsWithDateDividers(
        transactions: List<com.ivy.base.model.legacy.Transaction>,
        baseCurrencyCode: String,
        timeConverter: TimeConverter,
        getAccount: suspend (accountId: UUID) -> Account?,
        exchange: suspend (ExchangeData, BigDecimal) -> Option<BigDecimal>
    ): List<TransactionHistoryItem> {
        if (transactions.isEmpty()) return emptyList()

        return transactions
            .groupBy { with(timeConverter) { it.dateTime?.toLocalDate() } }
            .filterKeys { it != null }
            .toSortedMap { date1, date2 ->
                if (date1 == null || date2 == null) return@toSortedMap 0 // this case shouldn't happen
                (
                        date2.atStartOfDay().toEpochSeconds() - date1.atStartOfDay()
                            .toEpochSeconds()
                        ).toInt()
            }
            .flatMap { (date, transactionsForDate) ->
                val arg = ExchangeTrnArgument(
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

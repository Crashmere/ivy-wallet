package com.ivy.domain.usecase.category

import com.ivy.data.model.TransactionType
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.api.AccountStore
import com.ivy.data.model.FromToTimeRange
import com.ivy.data.api.TransactionStore
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import com.ivy.domain.usecase.exchange.sumInBaseCurrency
import com.ivy.domain.mapper.legacy.toLegacyTransaction
import com.ivy.domain.transaction.legacy.LegacyTransactionDateDividers
import com.ivy.domain.transaction.legacy.filterOverdueLegacy
import com.ivy.domain.transaction.legacy.filterUpcomingLegacy
import com.ivy.domain.time.nowUtc
import javax.inject.Inject

class GetUnspecifiedCategoryTransactionsSummaryUseCase @Inject constructor(
    private val accountStore: AccountStore,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeRatesUseCase: LegacyExchangeRatesUseCase,
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(range: FromToTimeRange): CategoryTransactionsSummary {
        val income = calculateUnspecifiedIncome(range)
        val expenses = calculateUnspecifiedExpenses(range)
        return CategoryTransactionsSummary(
            balance = income - expenses,
            income = income,
            expenses = expenses,
            history = historyUnspecified(range),
            upcoming = upcomingUnspecified(range),
            overdue = overdueUnspecified(range)
        )
    }

    private suspend fun calculateUnspecifiedIncome(range: FromToTimeRange): Double {
        return transactionStore
            .findAllUnspecifiedAndTypeAndBetween(
                type = TransactionType.INCOME,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacyTransaction() }
            .sumInBaseCurrency()
    }

    private suspend fun calculateUnspecifiedExpenses(range: FromToTimeRange): Double {
        return transactionStore
            .findAllUnspecifiedAndTypeAndBetween(
                type = TransactionType.EXPENSE,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacyTransaction() }
            .sumInBaseCurrency()
    }

    private suspend fun historyUnspecified(range: FromToTimeRange): List<TransactionHistoryItem> {
        return with(LegacyTransactionDateDividers) {
            transactionStore
                .findAllUnspecifiedAndBetween(
                    startDate = range.from(),
                    endDate = range.to()
                ).map { it.toLegacyTransaction() }
                .withDateDividers(
                    exchangeRatesUseCase = exchangeRatesUseCase,
                    baseCurrencyCode = getBaseCurrencyCode(),
                    accountStore = accountStore,
                )
        }
    }

    private suspend fun upcomingUnspecified(range: FromToTimeRange): CategoryDueTransactionsSummary {
        val transactions = transactionStore.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.upcomingFrom(nowUtc()),
            endDate = range.to()
        ).map {
            it.toLegacyTransaction()
        }.filterUpcomingLegacy()

        return CategoryDueTransactionsSummary(
            income = transactions.incomeInBaseCurrency(),
            expenses = transactions.expensesInBaseCurrency(),
            transactions = transactions
        )
    }

    private suspend fun overdueUnspecified(range: FromToTimeRange): CategoryDueTransactionsSummary {
        val transactions = transactionStore.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.from(),
            endDate = range.overdueTo(nowUtc())
        ).map {
            it.toLegacyTransaction()
        }.filterOverdueLegacy()

        return CategoryDueTransactionsSummary(
            income = transactions.incomeInBaseCurrency(),
            expenses = transactions.expensesInBaseCurrency(),
            transactions = transactions
        )
    }

    private suspend fun List<LegacyTransaction>.sumInBaseCurrency(): Double {
        return sumInBaseCurrency(
            exchangeRatesUseCase = exchangeRatesUseCase,
            baseCurrency = getBaseCurrencyCode(),
            accountStore = accountStore
        )
    }

    private suspend fun List<LegacyTransaction>.incomeInBaseCurrency(): Double {
        return filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency()
    }

    private suspend fun List<LegacyTransaction>.expensesInBaseCurrency(): Double {
        return filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency()
    }
}

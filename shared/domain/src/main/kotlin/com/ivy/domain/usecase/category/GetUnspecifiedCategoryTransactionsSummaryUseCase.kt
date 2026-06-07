package com.ivy.domain.usecase.category

import com.ivy.data.model.TransactionType
import com.ivy.data.model.legacy.Transaction
import com.ivy.data.model.legacy.TransactionHistoryItem
import com.ivy.data.api.AccountStore
import com.ivy.data.model.legacy.FromToTimeRange
import com.ivy.data.api.TransactionStore
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import com.ivy.domain.usecase.exchange.sumInBaseCurrency
import com.ivy.domain.mapper.legacy.toLegacy
import com.ivy.domain.transaction.legacy.LegacyTrnDateDividers
import com.ivy.domain.time.filterOverdueLegacy
import com.ivy.domain.time.filterUpcomingLegacy
import com.ivy.domain.time.nowUtc
import javax.inject.Inject

class GetUnspecifiedCategoryTransactionsSummaryUseCase @Inject constructor(
    private val accountStore: AccountStore,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeRatesUseCase: LegacyExchangeRatesUseCase,
    private val transactionRepository: TransactionStore,
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
        return transactionRepository
            .findAllUnspecifiedAndTypeAndBetween(
                type = TransactionType.INCOME,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy() }
            .sumInBaseCurrency()
    }

    private suspend fun calculateUnspecifiedExpenses(range: FromToTimeRange): Double {
        return transactionRepository
            .findAllUnspecifiedAndTypeAndBetween(
                type = TransactionType.EXPENSE,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy() }
            .sumInBaseCurrency()
    }

    private suspend fun historyUnspecified(range: FromToTimeRange): List<TransactionHistoryItem> {
        return with(LegacyTrnDateDividers) {
            transactionRepository
                .findAllUnspecifiedAndBetween(
                    startDate = range.from(),
                    endDate = range.to()
                ).map { it.toLegacy() }
                .withDateDividers(
                    exchangeRatesUseCase = exchangeRatesUseCase,
                    baseCurrencyCode = getBaseCurrencyCode(),
                    accountStore = accountStore,
                )
        }
    }

    private suspend fun upcomingUnspecified(range: FromToTimeRange): CategoryDueTransactionsSummary {
        val transactions = transactionRepository.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.upcomingFrom(nowUtc()),
            endDate = range.to()
        ).map {
            it.toLegacy()
        }.filterUpcomingLegacy()

        return CategoryDueTransactionsSummary(
            income = transactions.incomeInBaseCurrency(),
            expenses = transactions.expensesInBaseCurrency(),
            transactions = transactions
        )
    }

    private suspend fun overdueUnspecified(range: FromToTimeRange): CategoryDueTransactionsSummary {
        val transactions = transactionRepository.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.from(),
            endDate = range.overdueTo(nowUtc())
        ).map {
            it.toLegacy()
        }.filterOverdueLegacy()

        return CategoryDueTransactionsSummary(
            income = transactions.incomeInBaseCurrency(),
            expenses = transactions.expensesInBaseCurrency(),
            transactions = transactions
        )
    }

    private suspend fun List<Transaction>.sumInBaseCurrency(): Double {
        return sumInBaseCurrency(
            exchangeRatesUseCase = exchangeRatesUseCase,
            baseCurrency = getBaseCurrencyCode(),
            accountStore = accountStore
        )
    }

    private suspend fun List<Transaction>.incomeInBaseCurrency(): Double {
        return filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency()
    }

    private suspend fun List<Transaction>.expensesInBaseCurrency(): Double {
        return filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency()
    }
}

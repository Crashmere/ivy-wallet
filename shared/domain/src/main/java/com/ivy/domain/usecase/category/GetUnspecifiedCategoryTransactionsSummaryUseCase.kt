package com.ivy.domain.usecase.category

import com.ivy.base.model.TransactionType
import com.ivy.base.model.legacy.Transaction
import com.ivy.base.model.legacy.TransactionHistoryItem
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.model.legacy.FromToTimeRange
import com.ivy.data.repository.TransactionRepository
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.legacy.domain.logic.currency.ExchangeRatesLogic
import com.ivy.legacy.domain.logic.currency.sumInBaseCurrency
import com.ivy.legacy.domain.mapper.toLegacy
import com.ivy.legacy.domain.pure.transaction.LegacyTrnDateDividers
import com.ivy.legacy.domain.time.filterOverdueLegacy
import com.ivy.legacy.domain.time.filterUpcomingLegacy
import javax.inject.Inject

class GetUnspecifiedCategoryTransactionsSummaryUseCase @Inject constructor(
    private val accountDao: AccountDao,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeRatesLogic: ExchangeRatesLogic,
    private val transactionRepository: TransactionRepository,
    private val transactionMapper: TransactionMapper,
    private val timeProvider: TimeProvider,
    private val timeConverter: TimeConverter,
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
            ).map { it.toLegacy(transactionMapper) }
            .sumInBaseCurrency()
    }

    private suspend fun calculateUnspecifiedExpenses(range: FromToTimeRange): Double {
        return transactionRepository
            .findAllUnspecifiedAndTypeAndBetween(
                type = TransactionType.EXPENSE,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy(transactionMapper) }
            .sumInBaseCurrency()
    }

    private suspend fun historyUnspecified(range: FromToTimeRange): List<TransactionHistoryItem> {
        return with(LegacyTrnDateDividers) {
            transactionRepository
                .findAllUnspecifiedAndBetween(
                    startDate = range.from(),
                    endDate = range.to()
                ).map { it.toLegacy(transactionMapper) }
                .withDateDividers(
                    exchangeRatesLogic = exchangeRatesLogic,
                    baseCurrencyCode = getBaseCurrencyCode(),
                    accountDao = accountDao,
                    timeConverter = timeConverter,
                )
        }
    }

    private suspend fun upcomingUnspecified(range: FromToTimeRange): CategoryDueTransactionsSummary {
        val transactions = transactionRepository.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.upcomingFrom(timeProvider),
            endDate = range.to()
        ).map {
            it.toLegacy(transactionMapper)
        }.filterUpcomingLegacy(timeProvider, timeConverter)

        return CategoryDueTransactionsSummary(
            income = transactions.incomeInBaseCurrency(),
            expenses = transactions.expensesInBaseCurrency(),
            transactions = transactions
        )
    }

    private suspend fun overdueUnspecified(range: FromToTimeRange): CategoryDueTransactionsSummary {
        val transactions = transactionRepository.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        ).map {
            it.toLegacy(transactionMapper)
        }.filterOverdueLegacy(timeProvider, timeConverter)

        return CategoryDueTransactionsSummary(
            income = transactions.incomeInBaseCurrency(),
            expenses = transactions.expensesInBaseCurrency(),
            transactions = transactions
        )
    }

    private suspend fun List<Transaction>.sumInBaseCurrency(): Double {
        return sumInBaseCurrency(
            exchangeRatesLogic = exchangeRatesLogic,
            baseCurrency = getBaseCurrencyCode(),
            accountDao = accountDao
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

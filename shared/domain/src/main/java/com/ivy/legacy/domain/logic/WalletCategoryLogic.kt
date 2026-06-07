package com.ivy.legacy.domain.logic

import com.ivy.base.model.legacy.Transaction
import com.ivy.base.model.legacy.TransactionHistoryItem
import com.ivy.base.model.TransactionType
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.repository.TransactionRepository
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.legacy.domain.model.filterOverdue
import com.ivy.legacy.domain.model.filterOverdueLegacy
import com.ivy.legacy.domain.model.filterUpcoming
import com.ivy.legacy.domain.model.filterUpcomingLegacy
import com.ivy.legacy.domain.mapper.toLegacy
import com.ivy.legacy.domain.mapper.toLegacyDomain
import com.ivy.legacy.domain.pure.transaction.LegacyTrnDateDividers
import com.ivy.legacy.domain.logic.currency.ExchangeRatesLogic
import com.ivy.legacy.domain.logic.currency.sumInBaseCurrency
import java.util.UUID
import javax.inject.Inject

class WalletCategoryLogic @Inject constructor(
    private val accountDao: AccountDao,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeRatesLogic: ExchangeRatesLogic,
    private val transactionRepository: TransactionRepository,
    private val transactionMapper: TransactionMapper,
    private val timeProvider: TimeProvider,
    private val timeConverter: TimeConverter,
) {

    suspend fun calculateCategoryBalance(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange,
        accountFilterSet: Set<UUID> = emptySet(),
        transactions: List<Transaction> = emptyList()
    ): Double {
        val baseCurrency = getBaseCurrencyCode()
        val accounts = accountDao.findAll().map { it.toLegacyDomain() }

        return historyByCategory(
            category,
            range = range,
            accountFilterSet = accountFilterSet,
            transactions = transactions
        )
            .sumOf {
                val amount = exchangeRatesLogic.amountBaseCurrency(
                    transaction = it,
                    baseCurrency = baseCurrency,
                    accounts = accounts
                )

                when (it.type) {
                    TransactionType.INCOME -> amount
                    TransactionType.EXPENSE -> -amount
                    TransactionType.TRANSFER -> 0.0 // TODO: Transfer zero operation
                }
            }
    }

    suspend fun calculateCategoryIncome(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange,
        accountFilterSet: Set<UUID> = emptySet(),
    ): Double {
        return transactionRepository
            .findAllByCategoryAndTypeAndBetween(
                categoryId = category.id.value,
                type = TransactionType.INCOME,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy(transactionMapper) }
            .filter {
                accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
            }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

    suspend fun calculateCategoryIncome(
        incomeTransaction: List<Transaction>,
        accountFilterSet: Set<UUID> = emptySet()
    ): Double {
        return incomeTransaction
            .filter {
                accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
            }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

    suspend fun calculateCategoryExpenses(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange,
        accountFilterSet: Set<UUID> = emptySet(),
    ): Double {
        return transactionRepository
            .findAllByCategoryAndTypeAndBetween(
                categoryId = category.id.value,
                type = TransactionType.EXPENSE,
                startDate = range.from(),
                endDate = range.to()
            )
            .map {
                it.toLegacy(transactionMapper)
            }
            .filter {
                accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
            }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

    suspend fun calculateCategoryExpenses(
        expenseTransactions: List<Transaction>,
        accountFilterSet: Set<UUID> = emptySet()
    ): Double {
        return expenseTransactions
            .filter {
                accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
            }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

    suspend fun calculateUnspecifiedBalance(range: com.ivy.data.model.legacy.FromToTimeRange): Double {
        return calculateUnspecifiedIncome(range) - calculateUnspecifiedExpenses(range)
    }

    suspend fun calculateUnspecifiedIncome(range: com.ivy.data.model.legacy.FromToTimeRange): Double {
        return transactionRepository
            .findAllUnspecifiedAndTypeAndBetween(
                type = TransactionType.INCOME,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy(transactionMapper) }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

    suspend fun calculateUnspecifiedExpenses(range: com.ivy.data.model.legacy.FromToTimeRange): Double {
        return transactionRepository
            .findAllUnspecifiedAndTypeAndBetween(
                type = TransactionType.EXPENSE,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy(transactionMapper) }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

    suspend fun historyByCategoryAccountWithDateDividers(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange,
        accountFilterSet: Set<UUID>,
        transactions: List<Transaction> = emptyList()
    ): List<TransactionHistoryItem> {
        return with(LegacyTrnDateDividers) {
            historyByCategory(category, range, transactions = transactions)
                .filter {
                    accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
                }
                .withDateDividers(
                    exchangeRatesLogic = exchangeRatesLogic,
                    baseCurrencyCode = getBaseCurrencyCode(),
                    accountDao = accountDao,
                    timeConverter = timeConverter,
                )
        }
    }

    suspend fun historyByCategory(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange,
        accountFilterSet: Set<UUID> = emptySet(),
        transactions: List<Transaction> = emptyList()
    ): List<Transaction> {
        val trans = transactions.ifEmpty {
            transactionRepository
                .findAllByCategoryAndBetween(
                    categoryId = category.id.value,
                    startDate = range.from(),
                    endDate = range.to()
                ).map { it.toLegacy(transactionMapper) }
        }

        return trans.filter {
            accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
        }
    }

    suspend fun historyUnspecified(range: com.ivy.data.model.legacy.FromToTimeRange): List<TransactionHistoryItem> {
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

    suspend fun calculateUpcomingIncomeByCategory(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): Double {
        return upcomingByCategoryLegacy(category = category, range = range)
            .filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

    suspend fun calculateUpcomingExpensesByCategory(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): Double {
        return upcomingByCategoryLegacy(category = category, range = range)
            .filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

    suspend fun calculateUpcomingIncomeUnspecified(range: com.ivy.data.model.legacy.FromToTimeRange): Double {
        return upcomingUnspecifiedLegacy(range = range)
            .filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

    suspend fun calculateUpcomingExpensesUnspecified(range: com.ivy.data.model.legacy.FromToTimeRange): Double {
        return upcomingUnspecifiedLegacy(range = range)
            .filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

        suspend fun upcomingByCategoryLegacy(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): List<Transaction> {
        return transactionRepository.findAllDueToBetweenByCategory(
            categoryId = CategoryId(category.id.value),
            startDate = range.upcomingFrom(timeProvider),
            endDate = range.to()
        )
            .map {
                it.toLegacy(transactionMapper)
            }
            .filterUpcomingLegacy(timeProvider, timeConverter)
    }

    suspend fun upcomingByCategory(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): List<com.ivy.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByCategory(
            categoryId = CategoryId(category.id.value),
            startDate = range.upcomingFrom(timeProvider),
            endDate = range.to()
        ).filterUpcoming(timeProvider)
    }

        suspend fun upcomingUnspecifiedLegacy(range: com.ivy.data.model.legacy.FromToTimeRange): List<Transaction> {
        return transactionRepository.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.upcomingFrom(timeProvider),
            endDate = range.to()
        )
            .map {
                it.toLegacy(transactionMapper)
            }
            .filterUpcomingLegacy(timeProvider, timeConverter)
    }

    suspend fun upcomingUnspecified(
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): List<com.ivy.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.upcomingFrom(timeProvider),
            endDate = range.to()
        ).filterUpcoming(timeProvider)
    }

    suspend fun calculateOverdueIncomeByCategory(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): Double {
        return overdueByCategoryLegacy(category, range = range)
            .filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

    suspend fun calculateOverdueExpensesByCategory(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): Double {
        return overdueByCategoryLegacy(category, range = range)
            .filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

    suspend fun calculateOverdueIncomeUnspecified(range: com.ivy.data.model.legacy.FromToTimeRange): Double {
        return overdueUnspecifiedLegacy(range = range)
            .filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

    suspend fun calculateOverdueExpensesUnspecified(range: com.ivy.data.model.legacy.FromToTimeRange): Double {
        return overdueUnspecifiedLegacy(range = range)
            .filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrency = getBaseCurrencyCode(),
                accountDao = accountDao
            )
    }

        suspend fun overdueByCategoryLegacy(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): List<Transaction> {
        return transactionRepository.findAllDueToBetweenByCategory(
            categoryId = CategoryId(category.id.value),
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        )
            .map {
                it.toLegacy(transactionMapper)
            }
            .filterOverdueLegacy(timeProvider, timeConverter)
    }

    suspend fun overdueByCategory(
        category: Category,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): List<com.ivy.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByCategory(
            categoryId = CategoryId(category.id.value),
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        )
            .filterOverdue(timeProvider)
    }

        suspend fun overdueUnspecifiedLegacy(range: com.ivy.data.model.legacy.FromToTimeRange): List<Transaction> {
        return transactionRepository.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        )
            .map {
                it.toLegacy(transactionMapper)
            }
            .filterOverdueLegacy(timeProvider, timeConverter)
    }

    suspend fun overdueUnspecified(
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): List<com.ivy.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        ).filterOverdue(timeProvider)
    }
}

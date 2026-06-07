package com.ivy.domain.usecase.category

import com.ivy.data.model.TransactionType
import com.ivy.data.model.legacy.Transaction
import com.ivy.data.model.legacy.TransactionHistoryItem
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.data.api.AccountStore
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.legacy.FromToTimeRange
import com.ivy.data.api.TransactionStore
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import com.ivy.domain.usecase.exchange.sumInBaseCurrency
import com.ivy.domain.mapper.legacy.toLegacy
import com.ivy.domain.mapper.legacy.toLegacyDomain
import com.ivy.domain.transaction.legacy.LegacyTrnDateDividers
import com.ivy.domain.time.filterOverdueLegacy
import com.ivy.domain.time.filterUpcomingLegacy
import java.util.UUID
import javax.inject.Inject

class GetCategoryTransactionsSummaryUseCase @Inject constructor(
    private val accountStore: AccountStore,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeRatesLogic: LegacyExchangeRatesUseCase,
    private val transactionRepository: TransactionStore,
    private val timeProvider: TimeProvider,
    private val timeConverter: TimeConverter,
) {
    suspend operator fun invoke(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID> = emptySet(),
        providedTransactions: List<Transaction>? = null
    ): CategoryTransactionsSummary {
        val balanceTransactions = providedTransactions?.filter {
            it.type != TransactionType.TRANSFER && it.categoryId == category.id.value
        }
        val incomeTransactions = providedTransactions?.filter {
            it.categoryId == category.id.value && it.type == TransactionType.INCOME
        }
        val expenseTransactions = providedTransactions?.filter {
            it.categoryId == category.id.value && it.type == TransactionType.EXPENSE
        }

        return CategoryTransactionsSummary(
            balance = calculateCategoryBalance(
                category = category,
                range = range,
                accountFilterSet = accountFilterSet,
                transactions = balanceTransactions
            ),
            income = calculateCategoryIncome(
                category = category,
                range = range,
                accountFilterSet = accountFilterSet,
                transactions = incomeTransactions
            ),
            expenses = calculateCategoryExpenses(
                category = category,
                range = range,
                accountFilterSet = accountFilterSet,
                transactions = expenseTransactions
            ),
            history = historyByCategoryAccountWithDateDividers(
                category = category,
                range = range,
                accountFilterSet = accountFilterSet,
                transactions = balanceTransactions
            ),
            upcoming = upcomingByCategory(category = category, range = range),
            overdue = overdueByCategory(category = category, range = range)
        )
    }

    private suspend fun calculateCategoryBalance(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID>,
        transactions: List<Transaction>?
    ): Double {
        val baseCurrency = getBaseCurrencyCode()
        val accounts = accountStore.findAll().map { it.toLegacyDomain() }

        return historyByCategory(
            category = category,
            range = range,
            accountFilterSet = accountFilterSet,
            transactions = transactions
        ).sumOf {
            val amount = exchangeRatesLogic.amountBaseCurrency(
                transaction = it,
                baseCurrency = baseCurrency,
                accounts = accounts
            )

            when (it.type) {
                TransactionType.INCOME -> amount
                TransactionType.EXPENSE -> -amount
                TransactionType.TRANSFER -> 0.0
            }
        }
    }

    private suspend fun calculateCategoryIncome(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID>,
        transactions: List<Transaction>?
    ): Double {
        val incomeTransactions = transactions ?: transactionRepository
            .findAllByCategoryAndTypeAndBetween(
                categoryId = category.id.value,
                type = TransactionType.INCOME,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy() }

        return incomeTransactions.sumInBaseCurrency(accountFilterSet)
    }

    private suspend fun calculateCategoryExpenses(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID>,
        transactions: List<Transaction>?
    ): Double {
        val expenseTransactions = transactions ?: transactionRepository
            .findAllByCategoryAndTypeAndBetween(
                categoryId = category.id.value,
                type = TransactionType.EXPENSE,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy() }

        return expenseTransactions.sumInBaseCurrency(accountFilterSet)
    }

    private suspend fun historyByCategoryAccountWithDateDividers(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID>,
        transactions: List<Transaction>?
    ): List<TransactionHistoryItem> {
        return with(LegacyTrnDateDividers) {
            historyByCategory(
                category = category,
                range = range,
                accountFilterSet = accountFilterSet,
                transactions = transactions
            ).withDateDividers(
                exchangeRatesLogic = exchangeRatesLogic,
                baseCurrencyCode = getBaseCurrencyCode(),
                    accountStore = accountStore,
                timeConverter = timeConverter,
            )
        }
    }

    private suspend fun historyByCategory(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID>,
        transactions: List<Transaction>?
    ): List<Transaction> {
        val trans = transactions ?: transactionRepository
            .findAllByCategoryAndBetween(
                categoryId = category.id.value,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy() }

        return trans.filter {
            accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
        }
    }

    private suspend fun upcomingByCategory(
        category: Category,
        range: FromToTimeRange
    ): CategoryDueTransactionsSummary {
        val transactions = transactionRepository.findAllDueToBetweenByCategory(
            categoryId = CategoryId(category.id.value),
            startDate = range.upcomingFrom(timeProvider.utcNow()),
            endDate = range.to()
        ).map {
            it.toLegacy()
        }.filterUpcomingLegacy(timeProvider, timeConverter)

        return CategoryDueTransactionsSummary(
            income = transactions.incomeInBaseCurrency(),
            expenses = transactions.expensesInBaseCurrency(),
            transactions = transactions
        )
    }

    private suspend fun overdueByCategory(
        category: Category,
        range: FromToTimeRange
    ): CategoryDueTransactionsSummary {
        val transactions = transactionRepository.findAllDueToBetweenByCategory(
            categoryId = CategoryId(category.id.value),
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider.utcNow())
        ).map {
            it.toLegacy()
        }.filterOverdueLegacy(timeProvider, timeConverter)

        return CategoryDueTransactionsSummary(
            income = transactions.incomeInBaseCurrency(),
            expenses = transactions.expensesInBaseCurrency(),
            transactions = transactions
        )
    }

    private suspend fun List<Transaction>.sumInBaseCurrency(accountFilterSet: Set<UUID>): Double {
        return filter {
            accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
        }.sumInBaseCurrency(
            exchangeRatesLogic = exchangeRatesLogic,
            baseCurrency = getBaseCurrencyCode(),
            accountStore = accountStore
        )
    }

    private suspend fun List<Transaction>.incomeInBaseCurrency(): Double {
        return filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency(emptySet())
    }

    private suspend fun List<Transaction>.expensesInBaseCurrency(): Double {
        return filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency(emptySet())
    }
}

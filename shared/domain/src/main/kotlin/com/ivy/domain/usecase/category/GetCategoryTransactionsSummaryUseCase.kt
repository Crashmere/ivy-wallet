package com.ivy.domain.usecase.category

import com.ivy.data.model.TransactionType
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.api.AccountStore
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.FromToTimeRange
import com.ivy.data.api.TransactionStore
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import com.ivy.domain.usecase.exchange.sumInBaseCurrency
import com.ivy.domain.mapper.legacy.toLegacy
import com.ivy.domain.mapper.legacy.toLegacyDomain
import com.ivy.domain.transaction.legacy.LegacyTransactionDateDividers
import com.ivy.domain.transaction.legacy.filterOverdueLegacy
import com.ivy.domain.transaction.legacy.filterUpcomingLegacy
import com.ivy.domain.time.nowUtc
import java.util.UUID
import javax.inject.Inject

class GetCategoryTransactionsSummaryUseCase @Inject constructor(
    private val accountStore: AccountStore,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeRatesUseCase: LegacyExchangeRatesUseCase,
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID> = emptySet(),
        providedTransactions: List<LegacyTransaction>? = null
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
        transactions: List<LegacyTransaction>?
    ): Double {
        val baseCurrency = getBaseCurrencyCode()
        val accounts = accountStore.findAll().map { it.toLegacyDomain() }

        return historyByCategory(
            category = category,
            range = range,
            accountFilterSet = accountFilterSet,
            transactions = transactions
        ).sumOf {
            val amount = exchangeRatesUseCase.amountBaseCurrency(
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
        transactions: List<LegacyTransaction>?
    ): Double {
        val incomeTransactions = transactions ?: transactionStore
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
        transactions: List<LegacyTransaction>?
    ): Double {
        val expenseTransactions = transactions ?: transactionStore
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
        transactions: List<LegacyTransaction>?
    ): List<TransactionHistoryItem> {
        return with(LegacyTransactionDateDividers) {
            historyByCategory(
                category = category,
                range = range,
                accountFilterSet = accountFilterSet,
                transactions = transactions
            ).withDateDividers(
                exchangeRatesUseCase = exchangeRatesUseCase,
                baseCurrencyCode = getBaseCurrencyCode(),
                    accountStore = accountStore,
            )
        }
    }

    private suspend fun historyByCategory(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID>,
        transactions: List<LegacyTransaction>?
    ): List<LegacyTransaction> {
        val resolvedTransactions = transactions ?: transactionStore
            .findAllByCategoryAndBetween(
                categoryId = category.id.value,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy() }

        return resolvedTransactions.filter {
            accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
        }
    }

    private suspend fun upcomingByCategory(
        category: Category,
        range: FromToTimeRange
    ): CategoryDueTransactionsSummary {
        val transactions = transactionStore.findAllDueToBetweenByCategory(
            categoryId = CategoryId(category.id.value),
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

    private suspend fun overdueByCategory(
        category: Category,
        range: FromToTimeRange
    ): CategoryDueTransactionsSummary {
        val transactions = transactionStore.findAllDueToBetweenByCategory(
            categoryId = CategoryId(category.id.value),
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

    private suspend fun List<LegacyTransaction>.sumInBaseCurrency(accountFilterSet: Set<UUID>): Double {
        return filter {
            accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
        }.sumInBaseCurrency(
            exchangeRatesUseCase = exchangeRatesUseCase,
            baseCurrency = getBaseCurrencyCode(),
            accountStore = accountStore
        )
    }

    private suspend fun List<LegacyTransaction>.incomeInBaseCurrency(): Double {
        return filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency(emptySet())
    }

    private suspend fun List<LegacyTransaction>.expensesInBaseCurrency(): Double {
        return filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency(emptySet())
    }
}

package com.ivy.domain.usecase.category

import com.ivy.data.model.TransactionType
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.api.AccountStore
import com.ivy.data.model.Account
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.FromToTimeRange
import com.ivy.data.model.Transaction
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getFromValue
import com.ivy.data.model.getTransactionType
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.usecase.transaction.BuildTransactionHistoryItemsUseCase
import com.ivy.domain.time.nowUtc
import com.ivy.domain.time.todayStartOfLocalDayUtc
import java.util.UUID
import javax.inject.Inject

class GetCategoryTransactionsSummaryUseCase @Inject internal constructor(
    private val accountStore: AccountStore,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val buildTransactionHistoryItemsUseCase: BuildTransactionHistoryItemsUseCase,
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID> = emptySet(),
        providedTransactions: List<Transaction>? = null
    ): CategoryTransactionsSummary {
        val balanceTransactions = providedTransactions?.filter {
            it.getTransactionType() != TransactionType.TRANSFER && it.category == category.id
        }
        val incomeTransactions = providedTransactions?.filter {
            it.category == category.id && it.getTransactionType() == TransactionType.INCOME
        }
        val expenseTransactions = providedTransactions?.filter {
            it.category == category.id && it.getTransactionType() == TransactionType.EXPENSE
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
        val accounts = accountStore.findAll()

        return historyByCategory(
            category = category,
            range = range,
            accountFilterSet = accountFilterSet,
            transactions = transactions
        ).sumOf {
            val amount = it.amountBaseCurrency(
                baseCurrency = baseCurrency,
                accounts = accounts
            )

            when (it.getTransactionType()) {
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
        val incomeTransactions = transactions ?: transactionStore
            .findAllByCategoryAndTypeAndBetween(
                categoryId = category.id.value,
                type = TransactionType.INCOME,
                startDate = range.from(),
                endDate = range.to()
            )

        return incomeTransactions.sumInBaseCurrency(accountFilterSet)
    }

    private suspend fun calculateCategoryExpenses(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID>,
        transactions: List<Transaction>?
    ): Double {
        val expenseTransactions = transactions ?: transactionStore
            .findAllByCategoryAndTypeAndBetween(
                categoryId = category.id.value,
                type = TransactionType.EXPENSE,
                startDate = range.from(),
                endDate = range.to()
            )

        return expenseTransactions.sumInBaseCurrency(accountFilterSet)
    }

    private suspend fun historyByCategoryAccountWithDateDividers(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID>,
        transactions: List<Transaction>?
    ): List<TransactionHistoryItem> {
        return buildTransactionHistoryItemsUseCase(
            baseCurrency = getBaseCurrencyCode(),
            transactions = historyByCategory(
                category = category,
                range = range,
                accountFilterSet = accountFilterSet,
                transactions = transactions
            )
        )
    }

    private suspend fun historyByCategory(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID>,
        transactions: List<Transaction>?
    ): List<Transaction> {
        val resolvedTransactions = transactions ?: transactionStore
            .findAllByCategoryAndBetween(
                categoryId = category.id.value,
                startDate = range.from(),
                endDate = range.to()
            )

        return resolvedTransactions.filter {
            accountFilterSet.isEmpty() || accountFilterSet.contains(it.getFromAccount().value)
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
        ).filterUpcomingDueTransactions()

        return CategoryDueTransactionsSummary(
            income = transactions.transactionIncomeInBaseCurrency(),
            expenses = transactions.transactionExpensesInBaseCurrency(),
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
        ).filterOverdueDueTransactions()

        return CategoryDueTransactionsSummary(
            income = transactions.transactionIncomeInBaseCurrency(),
            expenses = transactions.transactionExpensesInBaseCurrency(),
            transactions = transactions
        )
    }

    private suspend fun List<Transaction>.sumInBaseCurrency(accountFilterSet: Set<UUID>): Double {
        val baseCurrency = getBaseCurrencyCode()
        val accounts = accountStore.findAll()
        return filter {
            accountFilterSet.isEmpty() || accountFilterSet.contains(it.getFromAccount().value)
        }.sumOf { it.amountBaseCurrency(baseCurrency, accounts) }
    }

    private suspend fun List<Transaction>.transactionIncomeInBaseCurrency(): Double {
        return filter { it.getTransactionType() == TransactionType.INCOME }
            .sumTransactionsInBaseCurrency()
    }

    private suspend fun List<Transaction>.transactionExpensesInBaseCurrency(): Double {
        return filter { it.getTransactionType() == TransactionType.EXPENSE }
            .sumTransactionsInBaseCurrency()
    }

    private suspend fun List<Transaction>.sumTransactionsInBaseCurrency(): Double {
        val baseCurrency = getBaseCurrencyCode()
        val accounts = accountStore.findAll()
        return sumOf { it.amountBaseCurrency(baseCurrency, accounts) }
    }

    private suspend fun Transaction.amountBaseCurrency(
        baseCurrency: String,
        accounts: List<Account>,
    ): Double {
        val amount = getFromValue().amount.value
        val amountCurrency = accounts.find { it.id == getFromAccount() }?.asset?.code
            ?: return amount
        return exchangeAmountUseCase(
            amount = amount.toBigDecimal(),
            baseCurrency = baseCurrency,
            fromCurrency = amountCurrency,
        ).getOrNull()?.toDouble() ?: amount
    }

    private fun Iterable<Transaction>.filterUpcomingDueTransactions(): List<Transaction> {
        val todayStartOfDayUtc = todayStartOfLocalDayUtc()
        return filter { !it.settled && it.time.isAfter(todayStartOfDayUtc) }
    }

    private fun Iterable<Transaction>.filterOverdueDueTransactions(): List<Transaction> {
        val todayStartOfDayUtc = todayStartOfLocalDayUtc()
        return filter { !it.settled && it.time.isBefore(todayStartOfDayUtc) }
    }
}

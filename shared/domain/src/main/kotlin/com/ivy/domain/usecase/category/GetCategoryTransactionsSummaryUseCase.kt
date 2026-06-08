package com.ivy.domain.usecase.category

import com.ivy.data.model.TransactionType
import com.ivy.data.model.legacy.LegacyTransaction
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
import com.ivy.domain.mapper.legacy.toLegacyTransaction
import com.ivy.domain.transaction.legacy.LegacyTransactionDateDividers
import com.ivy.domain.time.nowUtc
import com.ivy.domain.time.todayStartOfLocalDayUtc
import java.util.UUID
import javax.inject.Inject

class GetCategoryTransactionsSummaryUseCase @Inject internal constructor(
    private val accountStore: AccountStore,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
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
            ).map { it.toLegacyTransaction() }

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
            ).map { it.toLegacyTransaction() }

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
                exchangeAmountUseCase = exchangeAmountUseCase,
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
            ).map { it.toLegacyTransaction() }

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

    private suspend fun List<LegacyTransaction>.sumInBaseCurrency(accountFilterSet: Set<UUID>): Double {
        val baseCurrency = getBaseCurrencyCode()
        val accounts = accountStore.findAll()
        return filter {
            accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
        }.sumOf { it.amountBaseCurrency(baseCurrency, accounts) }
    }

    private suspend fun List<LegacyTransaction>.incomeInBaseCurrency(): Double {
        return filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency(emptySet())
    }

    private suspend fun List<LegacyTransaction>.expensesInBaseCurrency(): Double {
        return filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency(emptySet())
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

    private suspend fun LegacyTransaction.amountBaseCurrency(
        baseCurrency: String,
        accounts: List<Account>,
    ): Double {
        val amountCurrency = accounts.find { it.id.value == accountId }?.asset?.code
            ?: return amount.toDouble()
        return exchangeAmountUseCase(
            amount = amount,
            baseCurrency = baseCurrency,
            fromCurrency = amountCurrency,
        ).getOrNull()?.toDouble() ?: amount.toDouble()
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

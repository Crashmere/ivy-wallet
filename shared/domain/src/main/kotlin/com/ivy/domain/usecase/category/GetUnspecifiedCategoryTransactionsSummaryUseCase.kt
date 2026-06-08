package com.ivy.domain.usecase.category

import com.ivy.data.model.TransactionType
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.api.AccountStore
import com.ivy.data.model.Account
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
import javax.inject.Inject

class GetUnspecifiedCategoryTransactionsSummaryUseCase @Inject internal constructor(
    private val accountStore: AccountStore,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val buildTransactionHistoryItemsUseCase: BuildTransactionHistoryItemsUseCase,
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
            )
            .sumInBaseCurrency()
    }

    private suspend fun calculateUnspecifiedExpenses(range: FromToTimeRange): Double {
        return transactionStore
            .findAllUnspecifiedAndTypeAndBetween(
                type = TransactionType.EXPENSE,
                startDate = range.from(),
                endDate = range.to()
            )
            .sumInBaseCurrency()
    }

    private suspend fun historyUnspecified(range: FromToTimeRange): List<TransactionHistoryItem> {
        return buildTransactionHistoryItemsUseCase(
            baseCurrency = getBaseCurrencyCode(),
            transactions = transactionStore
                .findAllUnspecifiedAndBetween(
                    startDate = range.from(),
                    endDate = range.to()
                )
        )
    }

    private suspend fun upcomingUnspecified(range: FromToTimeRange): CategoryDueTransactionsSummary {
        val transactions = transactionStore.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.upcomingFrom(nowUtc()),
            endDate = range.to()
        ).filterUpcomingDueTransactions()

        return CategoryDueTransactionsSummary(
            income = transactions.transactionIncomeInBaseCurrency(),
            expenses = transactions.transactionExpensesInBaseCurrency(),
            transactions = transactions
        )
    }

    private suspend fun overdueUnspecified(range: FromToTimeRange): CategoryDueTransactionsSummary {
        val transactions = transactionStore.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.from(),
            endDate = range.overdueTo(nowUtc())
        ).filterOverdueDueTransactions()

        return CategoryDueTransactionsSummary(
            income = transactions.transactionIncomeInBaseCurrency(),
            expenses = transactions.transactionExpensesInBaseCurrency(),
            transactions = transactions
        )
    }

    private suspend fun List<Transaction>.sumInBaseCurrency(): Double {
        val baseCurrency = getBaseCurrencyCode()
        val accounts = accountStore.findAll()
        return sumOf { it.amountBaseCurrency(baseCurrency, accounts) }
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

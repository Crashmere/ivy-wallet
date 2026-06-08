package com.ivy.domain.usecase.budget

import com.ivy.data.api.AccountStore
import com.ivy.data.model.Budget
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.Account
import com.ivy.domain.usecase.exchange.ExchangeTransactionAmountUseCase
import javax.inject.Inject

class CalculateBudgetSpentAmountsUseCase @Inject internal constructor(
    private val accountStore: AccountStore,
    private val exchangeTransactionAmountUseCase: ExchangeTransactionAmountUseCase,
) {
    suspend operator fun invoke(
        budgets: List<Budget>,
        transactions: List<Transaction>,
        baseCurrencyCode: String,
    ): List<BudgetSpentAmount> {
        val accounts = accountStore.findAll()

        return budgets.map { budget ->
            BudgetSpentAmount(
                budget = budget,
                spentAmount = calculateSpentAmount(
                    budget = budget,
                    transactions = transactions,
                    accounts = accounts,
                    baseCurrencyCode = baseCurrencyCode
                )
            )
        }
    }

    private suspend fun calculateSpentAmount(
        budget: Budget,
        transactions: List<Transaction>,
        baseCurrencyCode: String,
        accounts: List<Account>,
    ): Double {
        val accountFilter = budget.parseAccountIds()
        val categoryFilter = budget.parseCategoryIds()

        var spentAmount = 0.0
        for (transaction in transactions
            .filter { accountFilter.isEmpty() || accountFilter.contains(it.getFromAccount().value) }
            .filter { categoryFilter.isEmpty() || categoryFilter.contains(it.category?.value) }) {
            spentAmount += when (transaction) {
                is Income -> 0.0
                is Expense -> exchangeTransactionAmountUseCase(
                    transaction = transaction,
                    accounts = accounts,
                    baseCurrency = baseCurrencyCode
                ).toDouble()
                is Transfer -> 0.0
            }
        }

        return spentAmount
    }
}

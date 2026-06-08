package com.ivy.domain.usecase.category

import com.ivy.data.api.AccountStore
import com.ivy.data.model.FromToTimeRange
import com.ivy.domain.mapper.legacy.toLegacyAccount
import com.ivy.domain.usecase.transaction.GetTransactionsForAccountsUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetCategoryMonthlyStatsUseCase @Inject internal constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val accountStore: AccountStore,
    private val getTransactionsForAccountsUseCase: GetTransactionsForAccountsUseCase,
    private val calculateCategoryIncomeWithAccountFiltersUseCase: CalculateCategoryIncomeWithAccountFiltersUseCase,
) {
    suspend operator fun invoke(
        range: FromToTimeRange,
        baseCurrency: String,
    ): List<CategoryMonthlyStats> {
        val accounts = accountStore.findAll().map { it.toLegacyAccount() }
        val transactions = getTransactionsForAccountsUseCase(
            range = range,
            accountIdFilterSet = accounts.map { it.id }.toHashSet()
        )

        return coroutineScope {
            getCategoriesUseCase().map { category ->
                async {
                    val incomeExpense = calculateCategoryIncomeWithAccountFiltersUseCase(
                        transactions = transactions,
                        accountFilterList = accounts,
                        category = category,
                        baseCurrency = baseCurrency
                    )

                    CategoryMonthlyStats(
                        category = category,
                        balance = (incomeExpense.income - incomeExpense.expense).toDouble(),
                        income = incomeExpense.income.toDouble(),
                        expenses = incomeExpense.expense.toDouble()
                    )
                }
            }.awaitAll()
        }
    }
}

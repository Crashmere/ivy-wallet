package com.ivy.domain.usecase.category

import com.ivy.data.model.legacy.Transaction
import com.ivy.data.model.Category
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.IncomeExpenseTransferPair
import com.ivy.domain.usecase.transaction.CalculateLegacyTransactionsIncomeExpenseUseCase
import javax.inject.Inject

class CalculateCategoryIncomeWithAccountFiltersUseCase @Inject constructor(
    private val calculateLegacyTransactionsIncomeExpenseUseCase: CalculateLegacyTransactionsIncomeExpenseUseCase
) {
    suspend operator fun invoke(
        transactions: List<Transaction>,
        accountFilterList: List<Account>,
        category: Category?,
        baseCurrency: String
    ): IncomeExpenseTransferPair {
        val accountFilterSet = accountFilterList.map { it.id }.toHashSet()
        val filteredTransactions = transactions
            .filter { it.categoryId == category?.id?.value }
            .filter {
                accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
            }

        return calculateLegacyTransactionsIncomeExpenseUseCase(
            transactions = filteredTransactions,
            baseCurrency = baseCurrency,
            accounts = accountFilterList
        )
    }
}

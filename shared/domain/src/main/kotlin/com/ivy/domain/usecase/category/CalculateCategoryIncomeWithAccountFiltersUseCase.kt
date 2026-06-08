package com.ivy.domain.usecase.category

import com.ivy.data.model.Category
import com.ivy.data.model.Transaction
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.IncomeExpenseTransferPair
import com.ivy.domain.usecase.transaction.CalculateTransactionsIncomeExpenseUseCase
import javax.inject.Inject

class CalculateCategoryIncomeWithAccountFiltersUseCase @Inject internal constructor(
    private val calculateTransactionsIncomeExpenseUseCase: CalculateTransactionsIncomeExpenseUseCase
) {
    suspend operator fun invoke(
        transactions: List<Transaction>,
        accountFilterList: List<LegacyAccount>,
        category: Category?,
        baseCurrency: String
    ): IncomeExpenseTransferPair {
        val accountFilterSet = accountFilterList.map { it.id }.toHashSet()
        val filteredTransactions = transactions
            .filter { it.category == category?.id }
            .filter {
                accountFilterSet.isEmpty() || accountFilterSet.contains(it.getFromAccount().value)
            }

        return calculateTransactionsIncomeExpenseUseCase(
            transactions = filteredTransactions,
            baseCurrency = baseCurrency,
            accounts = accountFilterList
        )
    }
}

package com.ivy.domain.usecase.category

import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.Category
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.IncomeExpenseTransferPair
import com.ivy.domain.usecase.transaction.CalculateLegacyTransactionsIncomeExpenseUseCase
import javax.inject.Inject

class CalculateCategoryIncomeWithAccountFiltersUseCase @Inject internal constructor(
    private val calculateLegacyTransactionsIncomeExpenseUseCase: CalculateLegacyTransactionsIncomeExpenseUseCase
) {
    suspend operator fun invoke(
        transactions: List<LegacyTransaction>,
        accountFilterList: List<LegacyAccount>,
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

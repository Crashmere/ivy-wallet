package com.ivy.domain.usecase.account

import com.ivy.data.model.AccountId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.legacy.FromToTimeRange
import com.ivy.domain.transaction.legacy.getValue
import javax.inject.Inject

class GetAccountOverdueTransactionsSummaryUseCase @Inject constructor(
    private val getAccountOverdueTransactionsUseCase: GetAccountOverdueTransactionsUseCase
) {
    suspend operator fun invoke(
        accountId: AccountId,
        range: FromToTimeRange
    ): AccountDueTransactionsSummary {
        val transactions = getAccountOverdueTransactionsUseCase(accountId, range)
        return AccountDueTransactionsSummary(
            income = transactions.filterIsInstance<Income>()
                .sumOf { it.getValue().toDouble() },
            expenses = transactions.filterIsInstance<Expense>()
                .sumOf { it.getValue().toDouble() },
            transactions = transactions
        )
    }
}

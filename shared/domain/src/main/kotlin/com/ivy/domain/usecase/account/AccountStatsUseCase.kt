package com.ivy.domain.usecase.account

import com.ivy.data.model.AccountId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import com.ivy.domain.model.StatSummary
import com.ivy.domain.model.StatSummaryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountStatsUseCase @Inject constructor() {
    suspend fun calculate(
        account: AccountId,
        transactions: List<Transaction>
    ): AccountStats = withContext(Dispatchers.Default) {
        val income = StatSummaryBuilder()
        val expense = StatSummaryBuilder()
        val transfersIn = StatSummaryBuilder()
        val transfersOut = StatSummaryBuilder()

        for (transaction in transactions) {
            when (transaction) {
                is Expense -> if (transaction.account == account) {
                    expense.process(transaction.value)
                }

                is Income -> if (transaction.account == account) {
                    income.process(transaction.value)
                }

                is Transfer -> {
                    when (account) {
                        transaction.fromAccount -> transfersOut.process(transaction.fromValue)
                        transaction.toAccount -> transfersIn.process(transaction.toValue)
                        else -> {
                            // ignore, not relevant transfer for the account
                        }
                    }
                }
            }
        }

        AccountStats(
            income = income.build(),
            expense = expense.build(),
            transfersIn = transfersIn.build(),
            transfersOut = transfersOut.build()
        )
    }
}

data class AccountStats(
    val income: StatSummary,
    val expense: StatSummary,
    val transfersIn: StatSummary,
    val transfersOut: StatSummary,
) {
    companion object {
        val Zero = AccountStats(
            income = StatSummary.Zero,
            expense = StatSummary.Zero,
            transfersIn = StatSummary.Zero,
            transfersOut = StatSummary.Zero,
        )
    }
}

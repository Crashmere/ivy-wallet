package com.ivy.domain.usecase.account

import arrow.core.nonEmptyListOf
import com.ivy.base.time.TimeProvider
import com.ivy.data.model.Account
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.data.model.legacy.IncomeExpensePair
import com.ivy.legacy.domain.pure.transaction.AccountValueFunctions
import com.ivy.legacy.domain.pure.transaction.foldTransactions
import java.math.BigDecimal
import javax.inject.Inject

class CalculateAccountIncomeExpenseUseCase @Inject constructor(
    private val getAccountTransactionsUseCase: GetAccountTransactionsUseCase,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(
        account: Account,
        range: ClosedTimeRange? = null,
        includeTransfersInCalc: Boolean = false
    ): IncomeExpensePair {
        val transactions = getAccountTransactionsUseCase(
            accountId = account.id,
            range = range ?: ClosedTimeRange.allTimeIvy(timeProvider)
        )
        val values = foldTransactions(
            transactions = transactions,
            arg = account.id.value,
            valueFunctions = nonEmptyListOf(
                AccountValueFunctions::income,
                AccountValueFunctions::expense,
                AccountValueFunctions::transferIncome,
                AccountValueFunctions::transferExpense
            )
        )
        return IncomeExpensePair(
            income = values[0] + if (includeTransfersInCalc) values[2] else BigDecimal.ZERO,
            expense = values[1] + if (includeTransfersInCalc) values[3] else BigDecimal.ZERO
        )
    }
}

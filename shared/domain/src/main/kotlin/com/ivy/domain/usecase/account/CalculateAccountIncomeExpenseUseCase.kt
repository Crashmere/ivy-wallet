package com.ivy.domain.usecase.account

import arrow.core.nonEmptyListOf
import com.ivy.data.model.Account
import com.ivy.data.model.ClosedTimeRange
import com.ivy.data.model.IncomeExpensePair
import com.ivy.domain.transaction.AccountValueFunctions
import com.ivy.domain.transaction.foldTransactions
import com.ivy.domain.time.nowUtc
import java.math.BigDecimal
import javax.inject.Inject

class CalculateAccountIncomeExpenseUseCase @Inject internal constructor(
    private val getAccountTransactionsUseCase: GetAccountTransactionsUseCase,
) {
    suspend operator fun invoke(
        account: Account,
        range: ClosedTimeRange? = null,
        includeTransfersInCalc: Boolean = false
    ): IncomeExpensePair {
        val transactions = getAccountTransactionsUseCase(
            accountId = account.id,
            range = range ?: ClosedTimeRange.allTimeIvy(nowUtc())
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

package com.ivy.legacy.domain.action.account

import arrow.core.nonEmptyListOf
import com.ivy.base.time.TimeProvider
import com.ivy.domain.usecase.account.GetAccountTransactionsUseCase
import com.ivy.legacy.frp.action.FPAction
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.data.model.legacy.IncomeExpensePair
import com.ivy.legacy.domain.pure.transaction.AccountValueFunctions
import com.ivy.legacy.domain.pure.transaction.foldTransactions
import java.math.BigDecimal
import javax.inject.Inject

class CalcAccIncomeExpenseAct @Inject constructor(
    private val getAccountTransactionsUseCase: GetAccountTransactionsUseCase,
    private val timeProvider: TimeProvider
) : FPAction<CalcAccIncomeExpenseAct.Input, CalcAccIncomeExpenseAct.Output>() {

    override suspend fun Input.compose(): suspend () -> Output = suspend {
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
        Output(
            account = account,
            incomeExpensePair = IncomeExpensePair(
                income = values[0] + if (includeTransfersInCalc) values[2] else BigDecimal.ZERO,
                expense = values[1] + if (includeTransfersInCalc) values[3] else BigDecimal.ZERO
            )
        )
    }

    @Suppress("DataClassDefaultValues")
    data class Input(
        val account: com.ivy.data.model.Account,
        val range: ClosedTimeRange? = null,
        val includeTransfersInCalc: Boolean = false
    )

    data class Output(
        val account: com.ivy.data.model.Account,
        val incomeExpensePair: IncomeExpensePair
    )
}

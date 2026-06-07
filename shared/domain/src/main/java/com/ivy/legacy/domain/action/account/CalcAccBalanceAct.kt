package com.ivy.legacy.domain.action.account

import arrow.core.nonEmptyListOf
import com.ivy.base.time.TimeProvider
import com.ivy.data.model.Account
import com.ivy.domain.usecase.account.GetAccountTransactionsUseCase
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.domain.pure.transaction.AccountValueFunctions
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.legacy.domain.pure.transaction.foldTransactions
import java.math.BigDecimal
import javax.inject.Inject

class CalcAccBalanceAct @Inject constructor(
    private val getAccountTransactionsUseCase: GetAccountTransactionsUseCase,
    private val timeProvider: TimeProvider,
) : FPAction<CalcAccBalanceAct.Input, CalcAccBalanceAct.Output>() {

    override suspend fun Input.compose(): suspend () -> Output = suspend {
        val transactions = getAccountTransactionsUseCase(
            accountId = account.id,
            range = range ?: ClosedTimeRange.allTimeIvy(timeProvider)
        )
        val balance = foldTransactions(
            transactions = transactions,
            arg = account.id.value,
            valueFunctions = nonEmptyListOf(AccountValueFunctions::balance)
        ).head
        Output(
            account = account, balance = balance
        )
    }

    @Suppress("DataClassDefaultValues")
    data class Input(
        val account: Account,
        val range: ClosedTimeRange? = null
    )

    data class Output(
        val account: Account,
        val balance: BigDecimal,
    )
}

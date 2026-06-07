package com.ivy.accounts

import arrow.core.toOption
import com.ivy.data.model.Account
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.legacy.domain.action.account.CalcAccBalanceAct
import com.ivy.legacy.domain.action.account.CalcAccIncomeExpenseAct
import com.ivy.legacy.domain.action.exchange.ExchangeAct
import com.ivy.legacy.domain.pure.exchange.ExchangeData
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.frp.action.thenMap
import com.ivy.legacy.frp.then
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class AccountDataAct @Inject constructor(
    private val exchangeAct: ExchangeAct,
    private val calcAccBalanceAct: CalcAccBalanceAct,
    private val calcAccIncomeExpenseAct: CalcAccIncomeExpenseAct
) : FPAction<AccountDataAct.Input, ImmutableList<AccountData>>() {

    override suspend fun Input.compose(): suspend () -> ImmutableList<AccountData> = suspend {
        accounts
    } thenMap { acc ->
        val balance = calcAccBalanceAct(
            CalcAccBalanceAct.Input(
                account = acc
            )
        ).balance

        val balanceBaseCurrency = if (acc.asset.code != baseCurrency) {
            exchangeAct(
                ExchangeAct.Input(
                    data = ExchangeData(
                        baseCurrency = baseCurrency,
                        fromCurrency = acc.asset.code.toOption()
                    ),
                    amount = balance
                )
            ).getOrNull()
        } else {
            null
        }

        val incomeExpensePair = calcAccIncomeExpenseAct(
            CalcAccIncomeExpenseAct.Input(
                account = acc,
                range = range,
                includeTransfersInCalc = includeTransfersInCalc
            )
        ).incomeExpensePair

        AccountData(
            account = acc,
            balance = balance.toDouble(),
            balanceBaseCurrency = balanceBaseCurrency?.toDouble(),
            monthlyIncome = incomeExpensePair.income.toDouble(),
            monthlyExpenses = incomeExpensePair.expense.toDouble(),
        )
    } then {
        it.toImmutableList()
    }

    data class Input(
        val accounts: ImmutableList<Account>,
        val baseCurrency: String,
        val range: ClosedTimeRange,
        val includeTransfersInCalc: Boolean = false
    )
}

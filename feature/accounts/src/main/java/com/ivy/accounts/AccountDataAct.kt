package com.ivy.accounts

import arrow.core.toOption
import com.ivy.data.model.Account
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.domain.usecase.account.CalculateAccountBalanceUseCase
import com.ivy.domain.usecase.account.CalculateAccountIncomeExpenseUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.legacy.domain.pure.exchange.ExchangeData
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.frp.action.thenMap
import com.ivy.legacy.frp.then
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class AccountDataAct @Inject constructor(
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val calculateAccountBalanceUseCase: CalculateAccountBalanceUseCase,
    private val calculateAccountIncomeExpenseUseCase: CalculateAccountIncomeExpenseUseCase
) : FPAction<AccountDataAct.Input, ImmutableList<AccountData>>() {

    override suspend fun Input.compose(): suspend () -> ImmutableList<AccountData> = suspend {
        accounts
    } thenMap { acc ->
        val balance = calculateAccountBalanceUseCase(acc)

        val balanceBaseCurrency = if (acc.asset.code != baseCurrency) {
            exchangeAmountUseCase(
                data = ExchangeData(
                    baseCurrency = baseCurrency,
                    fromCurrency = acc.asset.code.toOption()
                ),
                amount = balance
            ).getOrNull()
        } else {
            null
        }

        val incomeExpensePair = calculateAccountIncomeExpenseUseCase(
            account = acc,
            range = range,
            includeTransfersInCalc = includeTransfersInCalc
        )

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

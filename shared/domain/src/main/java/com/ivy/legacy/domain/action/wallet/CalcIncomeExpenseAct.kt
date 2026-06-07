package com.ivy.legacy.domain.action.wallet

import arrow.core.nonEmptyListOf
import arrow.core.toOption
import com.ivy.data.model.AccountId
import com.ivy.domain.usecase.account.GetAccountTransactionsUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.frp.action.thenMap
import com.ivy.legacy.frp.then
import com.ivy.data.model.legacy.Account
import com.ivy.legacy.domain.pure.account.filterExcluded
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.data.model.legacy.IncomeExpensePair
import com.ivy.legacy.domain.pure.exchange.ExchangeData
import com.ivy.legacy.domain.pure.transaction.AccountValueFunctions
import com.ivy.legacy.domain.pure.transaction.foldTransactions
import com.ivy.legacy.domain.pure.util.orZero
import timber.log.Timber
import javax.inject.Inject

class CalcIncomeExpenseAct @Inject constructor(
    private val getAccountTransactionsUseCase: GetAccountTransactionsUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase
) : FPAction<CalcIncomeExpenseAct.Input, IncomeExpensePair>() {

    override suspend fun Input.compose(): suspend () -> IncomeExpensePair = suspend {
        filterExcluded(accounts)
    } thenMap { acc ->
        Pair(
            acc,
            getAccountTransactionsUseCase(
                accountId = AccountId(acc.id),
                range = range
            )
        )
    } thenMap { (acc, trns) ->
        Timber.i("acc: $acc, trns = ${trns.size}")
        Pair(
            acc,
            foldTransactions(
                transactions = trns,
                valueFunctions = nonEmptyListOf(
                    AccountValueFunctions::income,
                    AccountValueFunctions::expense
                ),
                arg = acc.id
            )
        )
    } thenMap { (acc, stats) ->
        Timber.i("acc_stats: $acc - $stats")
        stats.map {
            exchangeAmountUseCase(
                data = ExchangeData(
                    baseCurrency = baseCurrency,
                    fromCurrency = (acc.currency ?: baseCurrency).toOption()
                ),
                amount = it
            ).orZero()
        }
    } then { statsList ->
        IncomeExpensePair(
            income = statsList.sumOf { it[0] },
            expense = statsList.sumOf { it[1] }
        )
    }

    data class Input(
        val baseCurrency: String,
        val accounts: List<Account>,
        val range: ClosedTimeRange,
    )
}

package com.ivy.legacy.domain.action.viewmodel.home

import com.ivy.base.time.TimeProvider
import com.ivy.data.model.Transaction
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.frp.then
import com.ivy.domain.usecase.account.GetLegacyAccountUseCase
import com.ivy.legacy.domain.action.exchange.ExchangeAct
import com.ivy.legacy.domain.action.exchange.actInput
import com.ivy.legacy.domain.action.transaction.DueTrnsAct
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.data.model.legacy.IncomeExpensePair
import com.ivy.legacy.domain.pure.exchange.ExchangeTrnArgument
import com.ivy.legacy.domain.pure.exchange.exchangeInBaseCurrency
import com.ivy.legacy.domain.pure.transaction.expenses
import com.ivy.legacy.domain.pure.transaction.incomes
import com.ivy.legacy.domain.pure.transaction.sumTrns
import java.time.LocalDate
import javax.inject.Inject

class DueTrnsInfoAct @Inject constructor(
    private val dueTrnsAct: DueTrnsAct,
    private val getLegacyAccountUseCase: GetLegacyAccountUseCase,
    private val exchangeAct: ExchangeAct,
    private val timeProvider: TimeProvider
) : FPAction<DueTrnsInfoAct.Input, DueTrnsInfoAct.Output>() {

    override suspend fun Input.compose(): suspend () -> Output =
        suspend {
            range
        } then dueTrnsAct then { trns ->
            val dateNow = timeProvider.localDateNow()
            trns.filter {
                this.dueFilter(it, dateNow)
            }
        } then { dueTrns ->
            // We have due transactions in different currencies
            val exchangeArg = ExchangeTrnArgument(
                baseCurrency = baseCurrency,
                exchange = ::actInput then exchangeAct,
                getAccount = { getLegacyAccountUseCase(it) }
            )

            Output(
                dueIncomeExpense = IncomeExpensePair(
                    income = sumTrns(
                        incomes(dueTrns),
                        ::exchangeInBaseCurrency,
                        exchangeArg
                    ),
                    expense = sumTrns(
                        expenses(dueTrns),
                        ::exchangeInBaseCurrency,
                        exchangeArg
                    )
                ),
                dueTrns = dueTrns
            )
        }

    data class Input(
        val range: ClosedTimeRange,
        val baseCurrency: String,
        val dueFilter: (Transaction, LocalDate) -> Boolean
    )

    data class Output(
        val dueIncomeExpense: IncomeExpensePair,
        val dueTrns: List<Transaction>
    )
}

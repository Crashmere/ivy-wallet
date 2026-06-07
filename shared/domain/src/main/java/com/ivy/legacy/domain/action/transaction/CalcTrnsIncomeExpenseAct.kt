package com.ivy.legacy.domain.action.transaction

import arrow.core.nonEmptyListOf
import com.ivy.data.model.Transaction
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.frp.then
import com.ivy.legacy.domain.model.Account
import com.ivy.legacy.domain.action.exchange.ExchangeAct
import com.ivy.legacy.domain.action.exchange.actInput
import com.ivy.data.model.legacy.IncomeExpenseTransferPair
import com.ivy.legacy.domain.pure.transaction.LegacyFoldTransactions
import com.ivy.legacy.domain.pure.transaction.WalletValueFunctions
import com.ivy.legacy.domain.pure.transaction.WalletValueFunctionsLegacy
import com.ivy.legacy.domain.pure.transaction.foldTransactionsSuspend
import javax.inject.Inject

class CalcTrnsIncomeExpenseAct @Inject constructor(
    private val exchangeAct: ExchangeAct
) : FPAction<CalcTrnsIncomeExpenseAct.Input, IncomeExpenseTransferPair>() {
    override suspend fun Input.compose(): suspend () -> IncomeExpenseTransferPair = suspend {
        foldTransactionsSuspend(
            transactions = transactions,
            valueFunctions = nonEmptyListOf(
                WalletValueFunctions::income,
                WalletValueFunctions::expense,
                WalletValueFunctions::transferIncome,
                WalletValueFunctions::transferExpenses
            ),
            arg = WalletValueFunctions.Argument(
                accounts = accounts,
                baseCurrency = baseCurrency,
                exchange = ::actInput then exchangeAct
            )
        )
    } then { values ->
        IncomeExpenseTransferPair(
            income = values[0],
            expense = values[1],
            transferIncome = values[2],
            transferExpense = values[3]
        )
    }

    data class Input(
        val transactions: List<Transaction>,
        val baseCurrency: String,
        val accounts: List<Account>
    )
}

class LegacyCalcTrnsIncomeExpenseAct @Inject constructor(
    private val exchangeAct: ExchangeAct
) : FPAction<LegacyCalcTrnsIncomeExpenseAct.Input, IncomeExpenseTransferPair>() {
    override suspend fun Input.compose(): suspend () -> IncomeExpenseTransferPair = suspend {
        LegacyFoldTransactions.foldTransactionsSuspend(
            transactions = transactions,
            valueFunctions = nonEmptyListOf(
                WalletValueFunctionsLegacy::income,
                WalletValueFunctionsLegacy::expense,
                WalletValueFunctionsLegacy::transferIncome,
                WalletValueFunctionsLegacy::transferExpenses
            ),
            arg = WalletValueFunctionsLegacy.Argument(
                accounts = accounts,
                baseCurrency = baseCurrency,
                exchange = ::actInput then exchangeAct
            )
        )
    } then { values ->
        IncomeExpenseTransferPair(
            income = values[0],
            expense = values[1],
            transferIncome = values[2],
            transferExpense = values[3]
        )
    }

    data class Input(
        val transactions: List<com.ivy.base.model.legacy.Transaction>,
        val baseCurrency: String,
        val accounts: List<Account>
    )
}

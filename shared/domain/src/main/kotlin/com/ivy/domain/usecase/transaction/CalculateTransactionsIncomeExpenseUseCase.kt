package com.ivy.domain.usecase.transaction

import arrow.core.nonEmptyListOf
import com.ivy.data.model.Account
import com.ivy.data.model.Transaction
import com.ivy.data.model.IncomeExpenseTransferPair
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.transaction.WalletValueFunctions
import com.ivy.domain.transaction.foldTransactionsSuspend
import javax.inject.Inject

class CalculateTransactionsIncomeExpenseUseCase @Inject internal constructor(
    private val exchangeAmountUseCase: ExchangeAmountUseCase
) {
    suspend operator fun invoke(
        transactions: List<Transaction>,
        baseCurrency: String,
        accounts: List<Account>
    ): IncomeExpenseTransferPair {
        val values = foldTransactionsSuspend(
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
                exchange = exchangeAmountUseCase::invoke
            )
        )

        return IncomeExpenseTransferPair(
            income = values[0],
            expense = values[1],
            transferIncome = values[2],
            transferExpense = values[3]
        )
    }
}

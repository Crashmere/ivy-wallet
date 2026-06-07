package com.ivy.domain.usecase.transaction

import arrow.core.nonEmptyListOf
import com.ivy.data.model.legacy.Transaction
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.IncomeExpenseTransferPair
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.transaction.legacy.LegacyFoldTransactions
import com.ivy.domain.transaction.legacy.WalletValueFunctionsLegacy
import javax.inject.Inject

class CalculateLegacyTransactionsIncomeExpenseUseCase @Inject constructor(
    private val exchangeAmountUseCase: ExchangeAmountUseCase
) {
    suspend operator fun invoke(
        transactions: List<Transaction>,
        baseCurrency: String,
        accounts: List<Account>
    ): IncomeExpenseTransferPair {
        val values = LegacyFoldTransactions.foldTransactionsSuspend(
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

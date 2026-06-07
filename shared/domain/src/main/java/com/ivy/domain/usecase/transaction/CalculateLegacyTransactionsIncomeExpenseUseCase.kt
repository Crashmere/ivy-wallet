package com.ivy.domain.usecase.transaction

import arrow.core.nonEmptyListOf
import com.ivy.base.model.legacy.Transaction
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.legacy.IncomeExpenseTransferPair
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.legacy.domain.pure.transaction.LegacyFoldTransactions
import com.ivy.legacy.domain.pure.transaction.WalletValueFunctionsLegacy
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

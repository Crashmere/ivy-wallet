package com.ivy.domain.usecase.transaction

import arrow.core.nonEmptyListOf
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.IncomeExpenseTransferPair
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.transaction.legacy.LegacyFoldTransactions
import com.ivy.domain.transaction.legacy.LegacyWalletValueFunctions
import javax.inject.Inject

class CalculateLegacyTransactionsIncomeExpenseUseCase @Inject constructor(
    private val exchangeAmountUseCase: ExchangeAmountUseCase
) {
    suspend operator fun invoke(
        transactions: List<LegacyTransaction>,
        baseCurrency: String,
        accounts: List<LegacyAccount>
    ): IncomeExpenseTransferPair {
        val values = LegacyFoldTransactions.foldTransactionsSuspend(
            transactions = transactions,
            valueFunctions = nonEmptyListOf(
                LegacyWalletValueFunctions::income,
                LegacyWalletValueFunctions::expense,
                LegacyWalletValueFunctions::transferIncome,
                LegacyWalletValueFunctions::transferExpenses
            ),
            arg = LegacyWalletValueFunctions.Argument(
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

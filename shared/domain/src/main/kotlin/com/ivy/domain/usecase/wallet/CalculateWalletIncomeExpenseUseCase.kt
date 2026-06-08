package com.ivy.domain.usecase.wallet

import arrow.core.nonEmptyListOf
import arrow.core.toOption
import com.ivy.data.model.Account
import com.ivy.data.model.ClosedTimeRange
import com.ivy.data.model.IncomeExpensePair
import com.ivy.domain.usecase.account.GetAccountTransactionsUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.exchange.ExchangeData
import com.ivy.domain.transaction.AccountValueFunctions
import com.ivy.domain.transaction.foldTransactions
import com.ivy.domain.util.orZero
import javax.inject.Inject

class CalculateWalletIncomeExpenseUseCase @Inject internal constructor(
    private val getAccountTransactionsUseCase: GetAccountTransactionsUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
) {
    suspend operator fun invoke(
        baseCurrency: String,
        accounts: List<Account>,
        range: ClosedTimeRange,
    ): IncomeExpensePair {
        val statsList = accounts.filter { it.includeInBalance }.map { account ->
            val transactions = getAccountTransactionsUseCase(
                accountId = account.id,
                range = range
            )
            val stats = foldTransactions(
                transactions = transactions,
                valueFunctions = nonEmptyListOf(
                    AccountValueFunctions::income,
                    AccountValueFunctions::expense
                ),
                arg = account.id.value
            )

            stats.map {
                exchangeAmountUseCase(
                    data = ExchangeData(
                        baseCurrency = baseCurrency,
                        fromCurrency = account.asset.code.toOption()
                    ),
                    amount = it
                ).orZero()
            }
        }

        return IncomeExpensePair(
            income = statsList.sumOf { it[0] },
            expense = statsList.sumOf { it[1] }
        )
    }
}

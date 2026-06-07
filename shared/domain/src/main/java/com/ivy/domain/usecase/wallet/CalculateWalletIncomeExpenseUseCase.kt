package com.ivy.domain.usecase.wallet

import arrow.core.nonEmptyListOf
import arrow.core.toOption
import com.ivy.data.model.AccountId
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.data.model.legacy.IncomeExpensePair
import com.ivy.domain.usecase.account.GetAccountTransactionsUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.account.filterExcluded
import com.ivy.domain.exchange.ExchangeData
import com.ivy.domain.transaction.legacy.AccountValueFunctions
import com.ivy.domain.transaction.legacy.foldTransactions
import com.ivy.domain.util.orZero
import javax.inject.Inject

class CalculateWalletIncomeExpenseUseCase @Inject constructor(
    private val getAccountTransactionsUseCase: GetAccountTransactionsUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
) {
    suspend operator fun invoke(
        baseCurrency: String,
        accounts: List<Account>,
        range: ClosedTimeRange,
    ): IncomeExpensePair {
        val statsList = filterExcluded(accounts).map { account ->
            val transactions = getAccountTransactionsUseCase(
                accountId = AccountId(account.id),
                range = range
            )
            val stats = foldTransactions(
                transactions = transactions,
                valueFunctions = nonEmptyListOf(
                    AccountValueFunctions::income,
                    AccountValueFunctions::expense
                ),
                arg = account.id
            )

            stats.map {
                exchangeAmountUseCase(
                    data = ExchangeData(
                        baseCurrency = baseCurrency,
                        fromCurrency = (account.currency ?: baseCurrency).toOption()
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

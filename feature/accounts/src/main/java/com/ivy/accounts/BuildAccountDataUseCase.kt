package com.ivy.accounts

import arrow.core.toOption
import com.ivy.data.model.Account
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.domain.usecase.account.CalculateAccountBalanceUseCase
import com.ivy.domain.usecase.account.CalculateAccountIncomeExpenseUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.exchange.ExchangeData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class BuildAccountDataUseCase @Inject constructor(
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val calculateAccountBalanceUseCase: CalculateAccountBalanceUseCase,
    private val calculateAccountIncomeExpenseUseCase: CalculateAccountIncomeExpenseUseCase
) {
    suspend operator fun invoke(
        accounts: ImmutableList<Account>,
        baseCurrency: String,
        range: ClosedTimeRange,
        includeTransfersInCalc: Boolean = false
    ): ImmutableList<AccountData> {
        return accounts.map { account ->
            val balance = calculateAccountBalanceUseCase(account)
            val balanceBaseCurrency = if (account.asset.code != baseCurrency) {
                exchangeAmountUseCase(
                    data = ExchangeData(
                        baseCurrency = baseCurrency,
                        fromCurrency = account.asset.code.toOption()
                    ),
                    amount = balance
                ).getOrNull()
            } else {
                null
            }
            val incomeExpensePair = calculateAccountIncomeExpenseUseCase(
                account = account,
                range = range,
                includeTransfersInCalc = includeTransfersInCalc
            )

            AccountData(
                account = account,
                balance = balance.toDouble(),
                balanceBaseCurrency = balanceBaseCurrency?.toDouble(),
                monthlyIncome = incomeExpensePair.income.toDouble(),
                monthlyExpenses = incomeExpensePair.expense.toDouble(),
            )
        }.toImmutableList()
    }
}

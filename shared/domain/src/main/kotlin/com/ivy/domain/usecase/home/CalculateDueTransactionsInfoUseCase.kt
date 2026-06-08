package com.ivy.domain.usecase.home

import com.ivy.data.model.Transaction
import com.ivy.data.model.ClosedTimeRange
import com.ivy.data.model.IncomeExpensePair
import com.ivy.domain.usecase.account.GetLegacyAccountUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.usecase.transaction.GetDueTransactionsUseCase
import com.ivy.domain.exchange.ExchangeTransactionArgument
import com.ivy.domain.exchange.exchangeInBaseCurrency
import com.ivy.domain.transaction.expenses
import com.ivy.domain.transaction.incomes
import com.ivy.domain.transaction.sumTransactions
import com.ivy.domain.time.nowLocalDate
import java.time.LocalDate
import javax.inject.Inject

internal class CalculateDueTransactionsInfoUseCase @Inject internal constructor(
    private val getDueTransactionsUseCase: GetDueTransactionsUseCase,
    private val getLegacyAccountUseCase: GetLegacyAccountUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
) {
    suspend operator fun invoke(
        range: ClosedTimeRange,
        baseCurrency: String,
        dueFilter: (Transaction, LocalDate) -> Boolean
    ): DueTransactionsInfo {
        val dateNow = nowLocalDate()
        val dueTransactions = getDueTransactionsUseCase(range)
            .filter { dueFilter(it, dateNow) }
        val exchangeArg = ExchangeTransactionArgument(
            baseCurrency = baseCurrency,
            exchange = exchangeAmountUseCase::invoke,
            getAccount = { getLegacyAccountUseCase(it) }
        )

        return DueTransactionsInfo(
            incomeExpense = IncomeExpensePair(
                income = sumTransactions(
                    incomes(dueTransactions),
                    ::exchangeInBaseCurrency,
                    exchangeArg
                ),
                expense = sumTransactions(
                    expenses(dueTransactions),
                    ::exchangeInBaseCurrency,
                    exchangeArg
                )
            ),
            transactions = dueTransactions
        )
    }
}

data class DueTransactionsInfo(
    val incomeExpense: IncomeExpensePair,
    val transactions: List<Transaction>
)

package com.ivy.domain.usecase.home

import com.ivy.base.time.TimeProvider
import com.ivy.data.model.Transaction
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.data.model.legacy.IncomeExpensePair
import com.ivy.domain.usecase.account.GetLegacyAccountUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.usecase.transaction.GetDueTransactionsUseCase
import com.ivy.legacy.domain.pure.exchange.ExchangeTrnArgument
import com.ivy.legacy.domain.pure.exchange.exchangeInBaseCurrency
import com.ivy.legacy.domain.pure.transaction.expenses
import com.ivy.legacy.domain.pure.transaction.incomes
import com.ivy.legacy.domain.pure.transaction.sumTrns
import java.time.LocalDate
import javax.inject.Inject

class CalculateDueTransactionsInfoUseCase @Inject constructor(
    private val getDueTransactionsUseCase: GetDueTransactionsUseCase,
    private val getLegacyAccountUseCase: GetLegacyAccountUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(
        range: ClosedTimeRange,
        baseCurrency: String,
        dueFilter: (Transaction, LocalDate) -> Boolean
    ): DueTransactionsInfo {
        val dateNow = timeProvider.localDateNow()
        val dueTransactions = getDueTransactionsUseCase(range)
            .filter { dueFilter(it, dateNow) }
        val exchangeArg = ExchangeTrnArgument(
            baseCurrency = baseCurrency,
            exchange = exchangeAmountUseCase::invoke,
            getAccount = { getLegacyAccountUseCase(it) }
        )

        return DueTransactionsInfo(
            incomeExpense = IncomeExpensePair(
                income = sumTrns(
                    incomes(dueTransactions),
                    ::exchangeInBaseCurrency,
                    exchangeArg
                ),
                expense = sumTrns(
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

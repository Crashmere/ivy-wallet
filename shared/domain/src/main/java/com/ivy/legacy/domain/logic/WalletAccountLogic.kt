package com.ivy.legacy.domain.logic

import arrow.core.getOrElse
import com.ivy.base.model.legacy.Transaction
import com.ivy.base.model.TransactionType
import com.ivy.base.time.TimeProvider
import com.ivy.data.model.AccountId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.repository.TransactionRepository
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.domain.usecase.currency.GetBaseCurrencyUseCase
import com.ivy.legacy.domain.action.account.CalcAccBalanceAct
import com.ivy.legacy.domain.model.Account
import com.ivy.legacy.domain.mapper.toDomain
import com.ivy.legacy.domain.pure.transaction.getValue
import com.ivy.legacy.domain.time.filterOverdue
import com.ivy.legacy.domain.time.filterUpcoming
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.absoluteValue

class WalletAccountLogic @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val transactionMapper: TransactionMapper,
    private val calcAccBalanceAct: CalcAccBalanceAct,
    private val getBaseCurrency: GetBaseCurrencyUseCase,
    private val timeProvider: TimeProvider
) {

    suspend fun adjustBalance(
        account: Account,
        actualBalance: Double? = null,
        newBalance: Double,

        adjustTransactionTitle: String = "Adjust balance",

        isFiat: Boolean? = null,
    ) {
        val ab = actualBalance ?: calculateAccountBalance(account)
        val diff = ab - newBalance

        val finalDiff = if (isFiat == true && abs(diff) < 0.009) 0.0 else diff
        when {
            finalDiff < 0 -> {
                // add income
                Transaction(
                    type = TransactionType.INCOME,
                    title = adjustTransactionTitle,
                    amount = diff.absoluteValue.toBigDecimal(),
                    toAmount = diff.absoluteValue.toBigDecimal(),
                    dateTime = timeProvider.utcNow(),
                    accountId = account.id,
                ).toDomain(transactionMapper)?.let {
                    transactionRepository.save(it)
                }
            }

            finalDiff > 0 -> {
                // add expense
                Transaction(
                    type = TransactionType.EXPENSE,
                    title = adjustTransactionTitle,
                    amount = diff.absoluteValue.toBigDecimal(),
                    toAmount = diff.absoluteValue.toBigDecimal(),
                    dateTime = timeProvider.utcNow(),
                    accountId = account.id,
                ).toDomain(transactionMapper)?.let {
                    transactionRepository.save(it)
                }
            }
        }
    }

    suspend fun calculateAccountBalance(
        account: Account
    ): Double {
        val baseCurrency = getBaseCurrency()
        val domainAccount = account.toDomainAccount(baseCurrency)
            .getOrElse { return 0.0 }

        return calcAccBalanceAct(
            CalcAccBalanceAct.Input(
                account = domainAccount
            )
        ).balance.toDouble()
    }

    suspend fun calculateUpcomingIncome(
        account: Account,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): Double =
        upcoming(account, range = range)
            .filterIsInstance<Income>()
            .sumOf { it.getValue().toDouble() }

    suspend fun calculateUpcomingExpenses(
        account: Account,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): Double =
        upcoming(account = account, range = range)
            .filterIsInstance<Expense>()
            .sumOf { it.getValue().toDouble() }

    suspend fun calculateOverdueIncome(
        account: Account,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): Double =
        overdue(account, range = range)
            .filterIsInstance<Income>()
            .sumOf { it.getValue().toDouble() }

    suspend fun calculateOverdueExpenses(
        account: Account,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): Double =
        overdue(account, range = range)
            .filterIsInstance<Expense>()
            .sumOf { it.getValue().toDouble() }

    suspend fun upcoming(
        account: Account,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): List<com.ivy.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByAccount(
            accountId = AccountId(account.id),
            startDate = range.upcomingFrom(timeProvider),
            endDate = range.to()
        ).filterUpcoming(timeProvider)
    }

    suspend fun overdue(
        account: Account,
        range: com.ivy.data.model.legacy.FromToTimeRange
    ): List<com.ivy.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByAccount(
            accountId = AccountId(account.id),
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        ).filterOverdue(timeProvider)
    }
}

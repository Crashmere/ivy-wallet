package com.ivy.domain.usecase.planned

import com.ivy.data.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.AccountStore
import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.model.IntervalType
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.legacy.PlannedPaymentRule
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import com.ivy.domain.mapper.legacy.toLegacyDomain
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class PlannedPaymentsOverview(
    val oneTime: List<PlannedPaymentRule>,
    val oneTimeIncome: Double,
    val oneTimeExpenses: Double,
    val recurring: List<PlannedPaymentRule>,
    val recurringIncome: Double,
    val recurringExpenses: Double
)

class GetPlannedPaymentsOverviewUseCase @Inject constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeRatesLogic: LegacyExchangeRatesUseCase,
    private val accountStore: AccountStore,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(): PlannedPaymentsOverview {
        return withContext(dispatchers.io) {
            val oneTime = plannedPaymentRuleStore.findAllByOneTime(oneTime = true)
            val recurring = plannedPaymentRuleStore.findAllByOneTime(oneTime = false)
            val baseCurrency = getBaseCurrencyCode()
            val accounts = accountStore.findAll().map { it.toLegacyDomain() }

            PlannedPaymentsOverview(
                oneTime = oneTime,
                oneTimeIncome = oneTime
                    .filter { it.type == TransactionType.INCOME }
                    .sumPlannedInBaseCurrency(
                        baseCurrency = baseCurrency,
                        accounts = accounts
                    ),
                oneTimeExpenses = oneTime
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumPlannedInBaseCurrency(
                        baseCurrency = baseCurrency,
                        accounts = accounts
                    ),
                recurring = recurring,
                recurringIncome = recurring
                    .filter { it.type == TransactionType.INCOME }
                    .sumRecurringForMonthInBaseCurrency(
                        baseCurrency = baseCurrency,
                        accounts = accounts
                    ),
                recurringExpenses = recurring
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumRecurringForMonthInBaseCurrency(
                        baseCurrency = baseCurrency,
                        accounts = accounts
                    )
            )
        }
    }

    private suspend fun Iterable<PlannedPaymentRule>.sumPlannedInBaseCurrency(
        baseCurrency: String,
        accounts: List<Account>
    ): Double =
        sumOf {
            exchangeRatesLogic.amountBaseCurrency(
                plannedPayment = it,
                baseCurrency = baseCurrency,
                accounts = accounts
            )
        }

    private suspend fun Iterable<PlannedPaymentRule>.sumRecurringForMonthInBaseCurrency(
        baseCurrency: String,
        accounts: List<Account>
    ): Double =
        sumOf {
            amountForMonthInBaseCurrency(
                plannedPayment = it,
                baseCurrency = baseCurrency,
                accounts = accounts
            )
        }

    private suspend fun amountForMonthInBaseCurrency(
        plannedPayment: PlannedPaymentRule,
        baseCurrency: String,
        accounts: List<Account>
    ): Double {
        val amountBaseCurrency = exchangeRatesLogic.amountBaseCurrency(
            plannedPayment = plannedPayment,
            baseCurrency = baseCurrency,
            accounts = accounts,
        )

        if (plannedPayment.oneTime) {
            return amountBaseCurrency
        }

        val intervalN = plannedPayment.intervalN ?: return amountBaseCurrency
        if (intervalN <= 0) {
            return amountBaseCurrency
        }

        return when (plannedPayment.intervalType) {
            IntervalType.DAY -> {
                val monthDiff = 1 / AVG_DAYS_IN_MONTH
                (amountBaseCurrency / monthDiff) / intervalN
            }

            IntervalType.WEEK -> {
                val monthDiff = 7 / AVG_DAYS_IN_MONTH
                (amountBaseCurrency / monthDiff) / intervalN
            }

            IntervalType.MONTH -> {
                amountBaseCurrency / intervalN
            }

            IntervalType.YEAR -> {
                amountBaseCurrency / (12 * intervalN)
            }

            null -> amountBaseCurrency
        }
    }

    private companion object {
        const val AVG_DAYS_IN_MONTH = 30.436875
    }
}

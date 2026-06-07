package com.ivy.domain.usecase.planned

import com.ivy.base.model.legacy.Transaction
import com.ivy.data.api.AccountStore
import com.ivy.data.model.legacy.PlannedPaymentRule
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.incrementDate
import com.ivy.domain.mapper.legacy.toDomain
import java.time.Instant
import javax.inject.Inject

class GeneratePlannedPaymentTransactionsUseCase @Inject constructor(
    private val accountStore: AccountStore,
    private val transactionRepository: TransactionStore
) {
    suspend operator fun invoke(rule: PlannedPaymentRule) {
        transactionRepository.deletedByRecurringRuleIdAndNoDateTime(
            recurringRuleId = rule.id
        )

        if (rule.oneTime) {
            generateOneTime(rule)
        } else {
            generateRecurring(rule)
        }
    }

    private suspend fun generateOneTime(rule: PlannedPaymentRule) {
        val transactions = transactionRepository.findAllByRecurringRuleId(
            recurringRuleId = rule.id
        )

        if (transactions.isEmpty()) {
            generateTransaction(rule, rule.startDate!!)
        }
    }

    @Suppress("MagicNumber")
    private suspend fun generateRecurring(rule: PlannedPaymentRule) {
        val startDate = rule.startDate!!
        val endDate = startDate.plusSeconds(94_608_000)

        val transactions = transactionRepository.findAllByRecurringRuleId(
            recurringRuleId = rule.id
        )
        var transactionsToSkip = transactions.size

        var generatedTransactions = 0

        var date = startDate
        while (date.isBefore(endDate)) {
            if (generatedTransactions >= GENERATED_INSTANCES_LIMIT) {
                break
            }

            if (transactionsToSkip > 0) {
                transactionsToSkip--
            } else {
                generateTransaction(
                    rule = rule,
                    dueDate = date
                )
                generatedTransactions++
            }

            val intervalN = rule.intervalN!!.toLong()
            date = rule.intervalType!!.incrementDate(
                date = date,
                intervalN = intervalN
            )
        }
    }

    private suspend fun generateTransaction(
        rule: PlannedPaymentRule,
        dueDate: Instant
    ) {
        Transaction(
            type = rule.type,
            accountId = rule.accountId,
            recurringRuleId = rule.id,
            categoryId = rule.categoryId,
            amount = rule.amount.toBigDecimal(),
            title = rule.title,
            description = rule.description,
            dueDate = dueDate,
            dateTime = null,
            toAccountId = null,
        ).toDomain(accountStore)?.let {
            transactionRepository.save(it)
        }
    }

    private companion object {
        const val GENERATED_INSTANCES_LIMIT = 72
    }
}

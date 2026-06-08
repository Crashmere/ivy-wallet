package com.ivy.domain.usecase.planned

import com.ivy.data.api.AccountStore
import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.PlannedPaymentRule
import com.ivy.data.model.PositiveValue
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionMetadata
import com.ivy.data.model.TransactionType
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.incrementDate
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.model.primitive.PositiveDouble
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

internal class GeneratePlannedPaymentTransactionsUseCase @Inject internal constructor(
    private val accountStore: AccountStore,
    private val transactionStore: TransactionStore
) {
    suspend operator fun invoke(rule: PlannedPaymentRule) {
        transactionStore.deleteByRecurringRuleIdAndNoDateTime(
            recurringRuleId = rule.id
        )

        if (rule.oneTime) {
            generateOneTime(rule)
        } else {
            generateRecurring(rule)
        }
    }

    private suspend fun generateOneTime(rule: PlannedPaymentRule) {
        val transactions = transactionStore.findAllByRecurringRuleId(
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

        val transactions = transactionStore.findAllByRecurringRuleId(
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
        rule.toDueTransaction(dueDate)?.let {
            transactionStore.save(it)
        }
    }

    private suspend fun PlannedPaymentRule.toDueTransaction(dueDate: Instant): Transaction? {
        val accountId = AccountId(accountId)
        val account = accountStore.findById(accountId) ?: return null
        val positiveAmount = PositiveDouble.from(amount).getOrNull() ?: return null
        val value = PositiveValue(
            amount = positiveAmount,
            asset = account.asset
        )
        val title = title?.let(NotBlankTrimmedString::from)?.getOrNull()
        val description = description?.let(NotBlankTrimmedString::from)?.getOrNull()
        val category = categoryId?.let(::CategoryId)
        val metadata = TransactionMetadata(
            recurringRuleId = id,
            paidForDateTime = null,
            loanId = null,
            loanRecordId = null
        )

        return when (type) {
            TransactionType.INCOME -> Income(
                id = TransactionId(UUID.randomUUID()),
                title = title,
                description = description,
                category = category,
                time = dueDate,
                settled = false,
                metadata = metadata,
                tags = emptyList(),
                value = value,
                account = accountId,
            )

            TransactionType.EXPENSE -> Expense(
                id = TransactionId(UUID.randomUUID()),
                title = title,
                description = description,
                category = category,
                time = dueDate,
                settled = false,
                metadata = metadata,
                tags = emptyList(),
                value = value,
                account = accountId,
            )

            TransactionType.TRANSFER -> null
        }
    }

    private companion object {
        const val GENERATED_INSTANCES_LIMIT = 72
    }
}

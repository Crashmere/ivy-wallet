package com.ivy.domain.usecase.planned

import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.domain.time.nowUtc
import java.util.UUID
import javax.inject.Inject

class PayOrSkipPlannedTransactionsByIdsUseCase @Inject internal constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        transactionIds: List<UUID>,
        skipTransaction: Boolean = false
    ): Int {
        val dueTransactions = transactionIds
            .mapNotNull { transactionStore.findById(TransactionId(it)) }
            .filter { !it.settled }

        if (dueTransactions.isEmpty()) return 0

        if (skipTransaction) {
            dueTransactions.forEach { transaction ->
                transactionStore.deleteById(transaction.id)
            }
        } else {
            val paidAt = nowUtc()
            dueTransactions.forEach { transaction ->
                transactionStore.save(
                    transaction.markPlannedPaymentPaid(
                        paidAt = paidAt,
                        paidFor = transaction.time
                    )
                )
            }
        }

        dueTransactions.deleteOneTimeRulesIfNeeded()
        return dueTransactions.size
    }

    private suspend fun List<Transaction>.deleteOneTimeRulesIfNeeded() {
        val plannedPaymentRules = mapNotNull { transaction ->
            transaction.metadata.recurringRuleId?.let {
                plannedPaymentRuleStore.findById(it)
            }
        }
        plannedPaymentRules.forEach { plannedPaymentRule ->
            if (plannedPaymentRule.oneTime) {
                plannedPaymentRuleStore.deleteById(plannedPaymentRule.id)
            }
        }
    }
}

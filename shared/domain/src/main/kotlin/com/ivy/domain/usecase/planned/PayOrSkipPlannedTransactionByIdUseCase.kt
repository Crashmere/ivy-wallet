package com.ivy.domain.usecase.planned

import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.domain.time.nowUtc
import java.util.UUID
import javax.inject.Inject

class PayOrSkipPlannedTransactionByIdUseCase @Inject internal constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        transactionId: UUID,
        skipTransaction: Boolean = false
    ): Boolean {
        val transaction = transactionStore.findById(TransactionId(transactionId)) ?: return false
        if (transaction.settled) return false

        if (skipTransaction) {
            transactionStore.deleteById(transaction.id)
        } else {
            transactionStore.save(
                transaction.markPlannedPaymentPaid(
                    paidAt = nowUtc(),
                    paidFor = transaction.time
                )
            )
        }

        deleteOneTimeRuleIfNeeded(transaction)
        return true
    }

    private suspend fun deleteOneTimeRuleIfNeeded(transaction: Transaction) {
        val plannedPaymentRule = transaction.metadata.recurringRuleId?.let {
            plannedPaymentRuleStore.findById(it)
        }
        if (plannedPaymentRule != null && plannedPaymentRule.oneTime) {
            plannedPaymentRuleStore.deleteById(plannedPaymentRule.id)
        }
    }
}

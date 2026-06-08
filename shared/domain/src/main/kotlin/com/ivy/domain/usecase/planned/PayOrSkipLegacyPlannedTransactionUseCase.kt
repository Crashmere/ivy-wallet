package com.ivy.domain.usecase.planned

import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.TransactionId
import com.ivy.domain.time.nowUtc
import javax.inject.Inject

class PayOrSkipLegacyPlannedTransactionUseCase @Inject internal constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        transaction: LegacyTransaction,
        skipTransaction: Boolean = false
    ): LegacyTransaction? {
        if (transaction.dueDate == null || transaction.dateTime != null) return null

        val paidTransaction = transaction.copy(
            paidFor = transaction.dueDate,
            dueDate = null,
            dateTime = nowUtc(),
        )

        val plannedPaymentRule = paidTransaction.recurringRuleId?.let {
            plannedPaymentRuleStore.findById(it)
        }

        if (skipTransaction) {
            transactionStore.deleteById(TransactionId(paidTransaction.id))
        } else {
            transactionStore.findById(TransactionId(transaction.id))?.markPlannedPaymentPaid(
                paidAt = paidTransaction.dateTime ?: nowUtc(),
                paidFor = transaction.dueDate
            )?.let {
                transactionStore.save(it)
            }
        }

        if (plannedPaymentRule != null && plannedPaymentRule.oneTime) {
            plannedPaymentRuleStore.deleteById(plannedPaymentRule.id)
        }

        return paidTransaction
    }
}

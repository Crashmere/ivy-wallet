package com.ivy.domain.usecase.planned

import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Transaction
import com.ivy.domain.transaction.settleNow
import javax.inject.Inject

class PayOrSkipPlannedTransactionUseCase @Inject constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        transaction: Transaction,
        skipTransaction: Boolean = false
    ): Transaction? {
        if (transaction.settled) return null

        val paidTransaction = transaction.settleNow()

        val plannedPaymentRule = paidTransaction.metadata.recurringRuleId?.let {
            plannedPaymentRuleStore.findById(it)
        }

        if (skipTransaction) {
            transactionStore.deleteById(paidTransaction.id)
        } else {
            transactionStore.save(paidTransaction)
        }

        if (plannedPaymentRule != null && plannedPaymentRule.oneTime) {
            plannedPaymentRuleStore.deleteById(plannedPaymentRule.id)
        }

        return paidTransaction
    }
}

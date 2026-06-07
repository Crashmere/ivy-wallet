package com.ivy.domain.usecase.planned

import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Transaction
import com.ivy.domain.transaction.settleNow
import javax.inject.Inject

class PayOrSkipPlannedTransactionsUseCase @Inject constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val transactionRepository: TransactionStore,
) {
    suspend operator fun invoke(
        transactions: List<Transaction>,
        skipTransaction: Boolean = false
    ): List<Transaction> {
        val paidTransactions: List<Transaction> =
            transactions.filter { it.settled }

        if (paidTransactions.isEmpty()) return emptyList()

        paidTransactions.map {
            it.settleNow()
        }

        val plannedPaymentRules = paidTransactions.map { transaction ->
            transaction.metadata.recurringRuleId?.let {
                plannedPaymentRuleStore.findById(it)
            }
        }

        if (skipTransaction) {
            paidTransactions.forEach { paidTransaction ->
                transactionRepository.deleteById(paidTransaction.id)
            }
        } else {
            paidTransactions.forEach { paidTransaction ->
                transactionRepository.save(paidTransaction)
            }
        }

        plannedPaymentRules.forEach { plannedPaymentRule ->
            if (plannedPaymentRule != null && plannedPaymentRule.oneTime) {
                plannedPaymentRuleStore.deleteById(plannedPaymentRule.id)
            }
        }

        return paidTransactions
    }
}

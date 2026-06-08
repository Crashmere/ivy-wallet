package com.ivy.domain.usecase.planned

import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.api.AccountStore
import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.TransactionId
import com.ivy.domain.mapper.legacy.toTransaction
import com.ivy.domain.time.nowUtc
import javax.inject.Inject

class PayOrSkipLegacyPlannedTransactionsUseCase @Inject constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val accountStore: AccountStore,
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        transactions: List<LegacyTransaction>,
        skipTransaction: Boolean = false
    ): List<LegacyTransaction> {
        val paidTransactions =
            transactions.filter { (it.dueDate == null || it.dateTime != null).not() }

        if (paidTransactions.isEmpty()) return emptyList()

        paidTransactions.map {
            it.copy(
                dueDate = null,
                dateTime = nowUtc(),
            )
        }

        val plannedPaymentRules = paidTransactions.map { transaction ->
            transaction.recurringRuleId?.let {
                plannedPaymentRuleStore.findById(it)
            }
        }

        if (skipTransaction) {
            paidTransactions.forEach { paidTransaction ->
                transactionStore.deleteById(TransactionId(paidTransaction.id))
            }
        } else {
            paidTransactions.forEach { paidTransaction ->
                paidTransaction.toTransaction(accountStore)?.let {
                    transactionStore.save(it)
                }
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

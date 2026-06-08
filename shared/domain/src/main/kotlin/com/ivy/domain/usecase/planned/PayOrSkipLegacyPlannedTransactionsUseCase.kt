package com.ivy.domain.usecase.planned

import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.api.AccountStore
import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.TransactionId
import com.ivy.domain.mapper.legacy.toTransaction
import javax.inject.Inject

class PayOrSkipLegacyPlannedTransactionsUseCase @Inject internal constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val accountStore: AccountStore,
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        transactions: List<LegacyTransaction>,
        skipTransaction: Boolean = false
    ): List<LegacyTransaction> {
        val dueTransactions =
            transactions.filter { (it.dueDate == null || it.dateTime != null).not() }

        if (dueTransactions.isEmpty()) return emptyList()

        val plannedPaymentRules = dueTransactions.map { transaction ->
            transaction.recurringRuleId?.let {
                plannedPaymentRuleStore.findById(it)
            }
        }

        if (skipTransaction) {
            dueTransactions.forEach { dueTransaction ->
                transactionStore.deleteById(TransactionId(dueTransaction.id))
            }
        } else {
            dueTransactions.forEach { dueTransaction ->
                dueTransaction.toTransaction(accountStore)?.let {
                    transactionStore.save(it)
                }
            }
        }

        plannedPaymentRules.forEach { plannedPaymentRule ->
            if (plannedPaymentRule != null && plannedPaymentRule.oneTime) {
                plannedPaymentRuleStore.deleteById(plannedPaymentRule.id)
            }
        }

        return dueTransactions
    }
}

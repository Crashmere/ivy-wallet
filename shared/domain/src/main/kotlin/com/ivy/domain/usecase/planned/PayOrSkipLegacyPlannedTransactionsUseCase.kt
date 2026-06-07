package com.ivy.domain.usecase.planned

import com.ivy.data.model.legacy.Transaction
import com.ivy.base.time.TimeProvider
import com.ivy.data.api.AccountStore
import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.TransactionId
import com.ivy.domain.mapper.legacy.toDomain
import javax.inject.Inject

class PayOrSkipLegacyPlannedTransactionsUseCase @Inject constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val accountStore: AccountStore,
    private val transactionRepository: TransactionStore,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(
        transactions: List<Transaction>,
        skipTransaction: Boolean = false
    ): List<Transaction> {
        val paidTransactions =
            transactions.filter { (it.dueDate == null || it.dateTime != null).not() }

        if (paidTransactions.isEmpty()) return emptyList()

        paidTransactions.map {
            it.copy(
                dueDate = null,
                dateTime = timeProvider.utcNow(),
            )
        }

        val plannedPaymentRules = paidTransactions.map { transaction ->
            transaction.recurringRuleId?.let {
                plannedPaymentRuleStore.findById(it)
            }
        }

        if (skipTransaction) {
            paidTransactions.forEach { paidTransaction ->
                transactionRepository.deleteById(TransactionId(paidTransaction.id))
            }
        } else {
            paidTransactions.forEach { paidTransaction ->
                paidTransaction.toDomain(accountStore)?.let {
                    transactionRepository.save(it)
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

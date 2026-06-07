package com.ivy.domain.usecase.planned

import com.ivy.data.model.legacy.Transaction
import com.ivy.data.api.AccountStore
import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.TransactionId
import com.ivy.domain.mapper.legacy.toDomain
import com.ivy.domain.time.nowUtc
import javax.inject.Inject

class PayOrSkipLegacyPlannedTransactionUseCase @Inject constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val accountStore: AccountStore,
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(
        transaction: Transaction,
        skipTransaction: Boolean = false
    ): Transaction? {
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
            paidTransaction.toDomain(accountStore)?.let {
                transactionStore.save(it)
            }
        }

        if (plannedPaymentRule != null && plannedPaymentRule.oneTime) {
            plannedPaymentRuleStore.deleteById(plannedPaymentRule.id)
        }

        return paidTransaction
    }
}

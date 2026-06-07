package com.ivy.legacy.domain.logic

import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.repository.TransactionRepository
import com.ivy.base.coroutines.ioThread
import com.ivy.legacy.domain.pure.transaction.settleNow
import javax.inject.Inject

class PlannedPaymentsLogic @Inject constructor(
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
    private val transactionRepository: TransactionRepository,
) {
    suspend fun payOrGet(
        transaction: com.ivy.data.model.Transaction,
        skipTransaction: Boolean = false,
        onUpdateUI: suspend (paidTransaction: com.ivy.data.model.Transaction) -> Unit
    ) {
        if (transaction.settled) return

        val paidTransaction = transaction.settleNow()

        val plannedPaymentRule = ioThread {
            paidTransaction.metadata.recurringRuleId?.let {
                plannedPaymentRuleDao.findById(it)
            }
        }

        ioThread {
            if (skipTransaction) {
                transactionRepository.deleteById(paidTransaction.id)
            } else {
                transactionRepository.save(paidTransaction)
            }

            if (plannedPaymentRule != null && plannedPaymentRule.oneTime) {
                // delete paid oneTime planned payment rules
                plannedPaymentRuleWriter.deleteById(plannedPaymentRule.id)
            }
        }

        onUpdateUI(paidTransaction)
    }

    suspend fun payOrGet(
        transactions: List<com.ivy.data.model.Transaction>,
        skipTransaction: Boolean = false,
        onUpdateUI: suspend (paidTransactions: List<com.ivy.data.model.Transaction>) -> Unit
    ) {
        val paidTransactions: List<com.ivy.data.model.Transaction> =
            transactions.filter { it.settled }

        if (paidTransactions.isEmpty()) return

        paidTransactions.map {
            it.settleNow()
        }

        val plannedPaymentRules = ioThread {
            paidTransactions.map { transaction ->
                transaction.metadata.recurringRuleId?.let {
                    plannedPaymentRuleDao.findById(it)
                }
            }
        }

        ioThread {
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
                    // delete paid oneTime planned payment rules
                    plannedPaymentRuleWriter.deleteById(plannedPaymentRule.id)
                }
            }
        }

        onUpdateUI(paidTransactions)
    }

}

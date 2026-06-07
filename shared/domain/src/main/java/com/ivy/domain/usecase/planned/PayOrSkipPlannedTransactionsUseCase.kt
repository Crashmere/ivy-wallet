package com.ivy.domain.usecase.planned

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.model.Transaction
import com.ivy.data.repository.TransactionRepository
import com.ivy.legacy.domain.pure.transaction.settleNow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PayOrSkipPlannedTransactionsUseCase @Inject constructor(
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
    private val transactionRepository: TransactionRepository,
    private val dispatchers: DispatchersProvider
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

        val plannedPaymentRules = withContext(dispatchers.io) {
            paidTransactions.map { transaction ->
                transaction.metadata.recurringRuleId?.let {
                    plannedPaymentRuleDao.findById(it)
                }
            }
        }

        withContext(dispatchers.io) {
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
                    plannedPaymentRuleWriter.deleteById(plannedPaymentRule.id)
                }
            }
        }

        return paidTransactions
    }
}

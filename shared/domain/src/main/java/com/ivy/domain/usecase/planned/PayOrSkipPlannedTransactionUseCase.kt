package com.ivy.domain.usecase.planned

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.model.Transaction
import com.ivy.data.api.TransactionStore
import com.ivy.domain.transaction.legacy.settleNow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PayOrSkipPlannedTransactionUseCase @Inject constructor(
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
    private val transactionRepository: TransactionStore,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(
        transaction: Transaction,
        skipTransaction: Boolean = false
    ): Transaction? {
        if (transaction.settled) return null

        val paidTransaction = transaction.settleNow()

        val plannedPaymentRule = withContext(dispatchers.io) {
            paidTransaction.metadata.recurringRuleId?.let {
                plannedPaymentRuleDao.findById(it)
            }
        }

        withContext(dispatchers.io) {
            if (skipTransaction) {
                transactionRepository.deleteById(paidTransaction.id)
            } else {
                transactionRepository.save(paidTransaction)
            }

            if (plannedPaymentRule != null && plannedPaymentRule.oneTime) {
                plannedPaymentRuleWriter.deleteById(plannedPaymentRule.id)
            }
        }

        return paidTransaction
    }
}

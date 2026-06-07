package com.ivy.domain.usecase.planned

import com.ivy.base.model.legacy.Transaction
import com.ivy.base.threading.DispatchersProvider
import com.ivy.base.time.TimeProvider
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.model.TransactionId
import com.ivy.data.repository.TransactionRepository
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.domain.mapper.legacy.toDomain
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PayOrSkipLegacyPlannedTransactionUseCase @Inject constructor(
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val transactionMapper: TransactionMapper,
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
    private val transactionRepository: TransactionRepository,
    private val timeProvider: TimeProvider,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(
        transaction: Transaction,
        skipTransaction: Boolean = false
    ): Transaction? {
        if (transaction.dueDate == null || transaction.dateTime != null) return null

        val paidTransaction = transaction.copy(
            paidFor = transaction.dueDate,
            dueDate = null,
            dateTime = timeProvider.utcNow(),
        )

        val plannedPaymentRule = withContext(dispatchers.io) {
            paidTransaction.recurringRuleId?.let {
                plannedPaymentRuleDao.findById(it)
            }
        }

        withContext(dispatchers.io) {
            if (skipTransaction) {
                transactionRepository.deleteById(TransactionId(paidTransaction.id))
            } else {
                paidTransaction.toDomain(transactionMapper)?.let {
                    transactionRepository.save(it)
                }
            }

            if (plannedPaymentRule != null && plannedPaymentRule.oneTime) {
                plannedPaymentRuleWriter.deleteById(plannedPaymentRule.id)
            }
        }

        return paidTransaction
    }
}

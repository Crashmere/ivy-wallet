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

class PayOrSkipLegacyPlannedTransactionsUseCase @Inject constructor(
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val transactionMapper: TransactionMapper,
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
    private val transactionRepository: TransactionRepository,
    private val timeProvider: TimeProvider,
    private val dispatchers: DispatchersProvider
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

        val plannedPaymentRules = withContext(dispatchers.io) {
            paidTransactions.map { transaction ->
                transaction.recurringRuleId?.let {
                    plannedPaymentRuleDao.findById(it)
                }
            }
        }

        withContext(dispatchers.io) {
            if (skipTransaction) {
                paidTransactions.forEach { paidTransaction ->
                    transactionRepository.deleteById(TransactionId(paidTransaction.id))
                }
            } else {
                paidTransactions.forEach { paidTransaction ->
                    paidTransaction.toDomain(transactionMapper)?.let {
                        transactionRepository.save(it)
                    }
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

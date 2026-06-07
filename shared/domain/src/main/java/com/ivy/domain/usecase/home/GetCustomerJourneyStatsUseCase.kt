package com.ivy.domain.usecase.home

import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.repository.TransactionRepository
import javax.inject.Inject

class GetCustomerJourneyStatsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao
) {
    suspend operator fun invoke(): CustomerJourneyStats {
        return CustomerJourneyStats(
            transactionCount = transactionRepository.countHappenedTransactions().value,
            plannedPaymentCount = plannedPaymentRuleDao.countPlannedPayments()
        )
    }
}

data class CustomerJourneyStats(
    val transactionCount: Long,
    val plannedPaymentCount: Long
)

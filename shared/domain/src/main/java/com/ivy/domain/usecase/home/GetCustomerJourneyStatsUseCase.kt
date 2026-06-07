package com.ivy.domain.usecase.home

import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.api.TransactionStore
import javax.inject.Inject

class GetCustomerJourneyStatsUseCase @Inject constructor(
    private val transactionRepository: TransactionStore,
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

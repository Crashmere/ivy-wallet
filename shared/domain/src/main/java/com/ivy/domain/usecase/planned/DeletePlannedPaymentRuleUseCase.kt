package com.ivy.domain.usecase.planned

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.api.TransactionStore
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class DeletePlannedPaymentRuleUseCase @Inject constructor(
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
    private val transactionRepository: TransactionStore,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(ruleId: UUID) {
        withContext(dispatchers.io) {
            plannedPaymentRuleWriter.deleteById(ruleId)
            transactionRepository.deletedByRecurringRuleIdAndNoDateTime(
                recurringRuleId = ruleId
            )
        }
    }
}

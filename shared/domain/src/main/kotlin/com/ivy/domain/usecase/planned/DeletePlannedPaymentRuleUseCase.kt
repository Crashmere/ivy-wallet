package com.ivy.domain.usecase.planned

import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.TransactionStore
import java.util.UUID
import javax.inject.Inject

class DeletePlannedPaymentRuleUseCase @Inject constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val transactionStore: TransactionStore,
) {
    suspend operator fun invoke(ruleId: UUID) {
        plannedPaymentRuleStore.deleteById(ruleId)
        transactionStore.deleteByRecurringRuleIdAndNoDateTime(
            recurringRuleId = ruleId
        )
    }
}

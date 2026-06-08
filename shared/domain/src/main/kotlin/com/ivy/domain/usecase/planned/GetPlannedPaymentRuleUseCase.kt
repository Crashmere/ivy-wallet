package com.ivy.domain.usecase.planned

import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.model.PlannedPaymentRule
import java.util.UUID
import javax.inject.Inject

class GetPlannedPaymentRuleUseCase @Inject internal constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
) {
    suspend operator fun invoke(ruleId: UUID): PlannedPaymentRule? {
        return plannedPaymentRuleStore.findById(ruleId)
    }
}

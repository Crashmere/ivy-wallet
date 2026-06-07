package com.ivy.domain.usecase.planned

import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.model.legacy.PlannedPaymentRule
import javax.inject.Inject

class SavePlannedPaymentRuleUseCase @Inject constructor(
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val generatePlannedPaymentTransactionsUseCase: GeneratePlannedPaymentTransactionsUseCase,
) {
    suspend operator fun invoke(rule: PlannedPaymentRule) {
        plannedPaymentRuleStore.save(rule)
        generatePlannedPaymentTransactionsUseCase(rule)
    }
}

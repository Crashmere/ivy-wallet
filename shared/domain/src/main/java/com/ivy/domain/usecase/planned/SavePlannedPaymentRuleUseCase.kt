package com.ivy.domain.usecase.planned

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.model.legacy.PlannedPaymentRule
import com.ivy.domain.mapper.legacy.toEntity
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SavePlannedPaymentRuleUseCase @Inject constructor(
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
    private val generatePlannedPaymentTransactionsUseCase: GeneratePlannedPaymentTransactionsUseCase,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(rule: PlannedPaymentRule) {
        withContext(dispatchers.io) {
            plannedPaymentRuleWriter.save(rule.toEntity())
        }
        generatePlannedPaymentTransactionsUseCase(rule)
    }
}

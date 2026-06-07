package com.ivy.domain.usecase.planned

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.legacy.domain.mapper.toLegacyDomain
import com.ivy.data.model.legacy.PlannedPaymentRule
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetPlannedPaymentRuleUseCase @Inject constructor(
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(ruleId: UUID): PlannedPaymentRule? {
        return withContext(dispatchers.io) {
            plannedPaymentRuleDao.findById(ruleId)?.toLegacyDomain()
        }
    }
}

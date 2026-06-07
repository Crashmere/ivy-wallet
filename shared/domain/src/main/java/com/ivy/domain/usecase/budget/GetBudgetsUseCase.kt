package com.ivy.domain.usecase.budget

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.BudgetDao
import com.ivy.data.model.legacy.Budget
import com.ivy.legacy.domain.mapper.toLegacyDomain
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetBudgetsUseCase @Inject constructor(
    private val budgetDao: BudgetDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(): List<Budget> {
        return withContext(dispatchers.io) {
            budgetDao.findAll().map { it.toLegacyDomain() }
        }
    }
}

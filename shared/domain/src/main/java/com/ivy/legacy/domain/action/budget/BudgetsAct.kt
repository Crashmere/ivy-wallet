package com.ivy.legacy.domain.action.budget

import com.ivy.data.db.dao.read.BudgetDao
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.frp.action.thenMap
import com.ivy.legacy.frp.then
import com.ivy.data.model.legacy.Budget
import com.ivy.legacy.domain.mapper.toLegacyDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class BudgetsAct @Inject constructor(
    private val budgetDao: BudgetDao
) : FPAction<Unit, ImmutableList<Budget>>() {
    override suspend fun Unit.compose(): suspend () -> ImmutableList<Budget> = suspend {
        budgetDao.findAll()
    } thenMap { it.toLegacyDomain() } then { it.toImmutableList() }
}

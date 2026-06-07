package com.ivy.legacy.domain.action.loan

import com.ivy.data.db.dao.read.LoanDao
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.frp.action.thenMap
import com.ivy.legacy.frp.then
import com.ivy.data.model.legacy.Loan
import com.ivy.legacy.domain.mapper.toLegacyDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class LoansAct @Inject constructor(
    private val loanDao: LoanDao
) : FPAction<Unit, ImmutableList<Loan>>() {
    override suspend fun Unit.compose(): suspend () -> ImmutableList<Loan> = suspend {
        loanDao.findAll()
    } thenMap { it.toLegacyDomain() } then { it.toImmutableList() }
}

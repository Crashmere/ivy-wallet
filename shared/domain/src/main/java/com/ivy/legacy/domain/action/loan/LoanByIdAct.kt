package com.ivy.legacy.domain.action.loan

import com.ivy.data.db.dao.read.LoanDao
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.domain.model.Loan
import com.ivy.legacy.domain.mapper.toLegacyDomain
import java.util.UUID
import javax.inject.Inject

class LoanByIdAct @Inject constructor(
    private val loanDao: LoanDao
) : FPAction<UUID, Loan?>() {
    override suspend fun UUID.compose(): suspend () -> Loan? = suspend {
        loanDao.findById(this)?.toLegacyDomain()
    }
}

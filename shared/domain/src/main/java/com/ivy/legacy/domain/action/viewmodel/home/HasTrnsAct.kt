package com.ivy.legacy.domain.action.viewmodel.home

import com.ivy.data.db.dao.read.TransactionDao
import com.ivy.legacy.frp.action.FPAction
import javax.inject.Inject

class HasTrnsAct @Inject constructor(
    private val transactionDao: TransactionDao
) : FPAction<Unit, Boolean>() {
    override suspend fun Unit.compose(): suspend () -> Boolean = suspend {
        io {
            transactionDao.hasAny()
        }
    }
}

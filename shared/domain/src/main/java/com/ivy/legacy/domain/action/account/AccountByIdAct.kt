package com.ivy.legacy.domain.action.account

import com.ivy.data.db.dao.read.AccountDao
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.frp.then
import com.ivy.legacy.domain.model.Account
import com.ivy.legacy.domain.mapper.toLegacyDomain
import java.util.UUID
import javax.inject.Inject

class AccountByIdAct @Inject constructor(
    private val accountDao: AccountDao
) : FPAction<UUID, Account?>() {
    override suspend fun UUID.compose(): suspend () -> Account? = suspend {
        this // accountId
    } then accountDao::findById then {
        it?.toLegacyDomain()
    }
}

package com.ivy.legacy.domain.action.account

import com.ivy.data.db.dao.read.AccountDao
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.domain.model.Account
import com.ivy.legacy.domain.mapper.toLegacyDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class AccountsAct @Inject constructor(
    private val accountDao: AccountDao
) : FPAction<Unit, ImmutableList<Account>>() {

    override suspend fun Unit.compose(): suspend () -> ImmutableList<Account> = suspend {
        io { accountDao.findAll().map { it.toLegacyDomain() }.toImmutableList() }
    }
}

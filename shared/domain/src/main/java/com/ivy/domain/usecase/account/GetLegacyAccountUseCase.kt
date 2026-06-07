package com.ivy.domain.usecase.account

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.model.legacy.Account
import com.ivy.legacy.domain.mapper.toLegacyDomain
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetLegacyAccountUseCase @Inject constructor(
    private val accountDao: AccountDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(accountId: UUID): Account? {
        return withContext(dispatchers.io) {
            accountDao.findById(accountId)?.toLegacyDomain()
        }
    }
}

package com.ivy.domain.usecase.account

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.AccountStore
import com.ivy.data.model.AccountId
import com.ivy.data.model.legacy.Account
import com.ivy.domain.mapper.legacy.toLegacyDomain
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetLegacyAccountUseCase @Inject constructor(
    private val accountStore: AccountStore,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(accountId: UUID): Account? {
        return withContext(dispatchers.io) {
            accountStore.findById(AccountId(accountId))?.toLegacyDomain()
        }
    }
}

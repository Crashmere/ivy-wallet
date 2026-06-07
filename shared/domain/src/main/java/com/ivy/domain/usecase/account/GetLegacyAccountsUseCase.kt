package com.ivy.domain.usecase.account

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.AccountStore
import com.ivy.data.model.legacy.Account
import com.ivy.domain.mapper.legacy.toLegacyDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetLegacyAccountsUseCase @Inject constructor(
    private val accountStore: AccountStore,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(): ImmutableList<Account> {
        return withContext(dispatchers.io) {
            accountStore.findAll()
                .map { it.toLegacyDomain() }
                .toImmutableList()
        }
    }
}

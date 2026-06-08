package com.ivy.domain.usecase.account

import com.ivy.data.api.AccountStore
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.domain.mapper.legacy.toLegacyAccount
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetLegacyAccountsUseCase @Inject constructor(
    private val accountStore: AccountStore,
) {
    suspend operator fun invoke(): ImmutableList<LegacyAccount> {
        return withContext(Dispatchers.IO) {
            accountStore.findAll()
                .map { it.toLegacyAccount() }
                .toImmutableList()
        }
    }
}

package com.ivy.domain.usecase.account

import com.ivy.data.api.AccountStore
import com.ivy.data.model.AccountId
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.domain.mapper.legacy.toLegacyAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetLegacyAccountUseCase @Inject constructor(
    private val accountStore: AccountStore,
) {
    suspend operator fun invoke(accountId: UUID): LegacyAccount? {
        return withContext(Dispatchers.IO) {
            accountStore.findById(AccountId(accountId))?.toLegacyAccount()
        }
    }
}

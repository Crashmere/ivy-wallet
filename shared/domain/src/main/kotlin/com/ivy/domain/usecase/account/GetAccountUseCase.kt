package com.ivy.domain.usecase.account

import com.ivy.data.api.AccountStore
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import javax.inject.Inject

class GetAccountUseCase @Inject constructor(
    private val accountStore: AccountStore
) {
    suspend operator fun invoke(accountId: AccountId): Account? {
        return accountStore.findById(accountId)
    }
}

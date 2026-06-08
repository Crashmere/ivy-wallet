package com.ivy.domain.usecase.account

import com.ivy.data.api.AccountStore
import com.ivy.data.model.Account
import javax.inject.Inject

class SaveAccountUseCase @Inject internal constructor(
    private val accountStore: AccountStore
) {
    suspend operator fun invoke(account: Account) {
        accountStore.save(account)
    }
}

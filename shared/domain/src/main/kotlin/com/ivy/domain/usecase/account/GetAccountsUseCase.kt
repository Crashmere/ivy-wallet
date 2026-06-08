package com.ivy.domain.usecase.account

import com.ivy.data.api.AccountStore
import com.ivy.data.model.Account
import javax.inject.Inject

class GetAccountsUseCase @Inject internal constructor(
    private val accountStore: AccountStore
) {
    suspend operator fun invoke(): List<Account> {
        return accountStore.findAll()
    }
}

package com.ivy.domain.usecase.account

import com.ivy.data.model.Account
import com.ivy.data.repository.AccountRepository
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(): List<Account> {
        return accountRepository.findAll()
    }
}

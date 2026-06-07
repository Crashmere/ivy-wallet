package com.ivy.domain.usecase.account

import com.ivy.data.model.Account
import com.ivy.data.repository.AccountRepository
import javax.inject.Inject

class SaveAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(account: Account) {
        accountRepository.save(account)
    }
}

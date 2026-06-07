package com.ivy.domain.usecase.account

import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.repository.AccountRepository
import javax.inject.Inject

class GetAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(accountId: AccountId): Account? {
        return accountRepository.findById(accountId)
    }
}

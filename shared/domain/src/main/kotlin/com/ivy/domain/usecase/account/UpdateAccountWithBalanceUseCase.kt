package com.ivy.domain.usecase.account

import com.ivy.data.model.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateAccountWithBalanceUseCase @Inject internal constructor(
    private val adjustAccountBalanceUseCase: AdjustAccountBalanceUseCase,
    private val calculateAccountBalanceUseCase: CalculateAccountBalanceUseCase,
    private val saveAccountUseCase: SaveAccountUseCase,
) {
    suspend operator fun invoke(
        account: Account,
        newBalance: Double
    ) {
        withContext(Dispatchers.IO) {
            saveAccountUseCase(account)

            adjustAccountBalanceUseCase(
                account = account,
                actualBalance = calculateAccountBalanceUseCase(account).toDouble(),
                newBalance = newBalance
            )
        }
    }
}

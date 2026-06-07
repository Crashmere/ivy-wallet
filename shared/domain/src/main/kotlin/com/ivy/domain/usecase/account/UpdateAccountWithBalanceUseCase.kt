package com.ivy.domain.usecase.account

import com.ivy.domain.usecase.currency.GetBaseCurrencyUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.ivy.data.model.legacy.Account as LegacyAccount

class UpdateAccountWithBalanceUseCase @Inject constructor(
    private val adjustAccountBalanceUseCase: AdjustAccountBalanceUseCase,
    private val calculateAccountBalanceUseCase: CalculateAccountBalanceUseCase,
    private val saveAccountUseCase: SaveAccountUseCase,
    private val getBaseCurrency: GetBaseCurrencyUseCase,
) {
    suspend operator fun invoke(
        account: LegacyAccount,
        newBalance: Double
    ) {
        withContext(Dispatchers.IO) {
            val domainAccount = account.toDomainAccount(getBaseCurrency()).getOrNull()
                ?: return@withContext
            saveAccountUseCase(domainAccount)

            adjustAccountBalanceUseCase(
                account = account,
                actualBalance = calculateAccountBalanceUseCase(domainAccount).toDouble(),
                newBalance = newBalance
            )
        }
    }
}

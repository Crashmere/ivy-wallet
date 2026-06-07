package com.ivy.domain.usecase.account

import com.ivy.base.threading.DispatchersProvider
import com.ivy.domain.usecase.currency.GetBaseCurrencyUseCase
import com.ivy.legacy.domain.logic.WalletAccountLogic
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.ivy.data.model.legacy.Account as LegacyAccount

class UpdateAccountWithBalanceUseCase @Inject constructor(
    private val accountLogic: WalletAccountLogic,
    private val saveAccountUseCase: SaveAccountUseCase,
    private val getBaseCurrency: GetBaseCurrencyUseCase,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(
        account: LegacyAccount,
        newBalance: Double
    ) {
        withContext(dispatchers.io) {
            val domainAccount = account.toDomainAccount(getBaseCurrency()).getOrNull()
                ?: return@withContext
            saveAccountUseCase(domainAccount)

            accountLogic.adjustBalance(
                account = account,
                actualBalance = accountLogic.calculateAccountBalance(account),
                newBalance = newBalance
            )
        }
    }
}

package com.ivy.domain.usecase.account

import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.primitive.AssetCode
import com.ivy.domain.mapper.legacy.toDomainAccount
import javax.inject.Inject

class SaveLegacyAccountUseCase @Inject internal constructor(
    private val saveAccountUseCase: SaveAccountUseCase,
) {
    suspend operator fun invoke(
        account: LegacyAccount,
        baseCurrency: AssetCode,
    ): Boolean {
        val domainAccount = account.toDomainAccount(baseCurrency).getOrNull()
            ?: return false
        saveAccountUseCase(domainAccount)
        return true
    }
}

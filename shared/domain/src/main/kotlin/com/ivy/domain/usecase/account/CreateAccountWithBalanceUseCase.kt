package com.ivy.domain.usecase.account

import arrow.core.raise.either
import com.ivy.data.api.AccountStore
import com.ivy.data.model.AccountId
import com.ivy.data.model.CreateAccountData
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.util.nextOrderNum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import com.ivy.data.model.Account as DomainAccount
import com.ivy.data.model.legacy.Account as LegacyAccount

class CreateAccountWithBalanceUseCase @Inject constructor(
    private val adjustAccountBalanceUseCase: AdjustAccountBalanceUseCase,
    private val accountStore: AccountStore,
    private val saveAccountUseCase: SaveAccountUseCase,
) {
    suspend operator fun invoke(data: CreateAccountData) {
        withContext(Dispatchers.IO) {
            val account = either {
                DomainAccount(
                    id = AccountId(value = UUID.randomUUID()),
                    name = NotBlankTrimmedString.from(data.name).bind(),
                    asset = AssetCode.from(data.currency).bind(),
                    color = ColorInt(data.color),
                    icon = data.icon?.let(IconAsset::from)?.getOrNull(),
                    includeInBalance = data.includeBalance,
                    orderNum = accountStore.findMaxOrderNum().nextOrderNum(),
                )
            }.getOrNull() ?: return@withContext

            saveAccountUseCase(account)

            val legacyAccount = LegacyAccount(
                name = data.name,
                currency = data.currency,
                color = data.color,
                icon = data.icon,
                includeInBalance = data.includeBalance,
                orderNum = account.orderNum,
                id = account.id.value
            )
            adjustAccountBalanceUseCase(
                account = legacyAccount,
                actualBalance = 0.0,
                newBalance = data.balance
            )
        }
    }
}

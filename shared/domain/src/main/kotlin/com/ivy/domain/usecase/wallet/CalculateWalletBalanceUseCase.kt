package com.ivy.domain.usecase.wallet

import arrow.core.toOption
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.ClosedTimeRange
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.usecase.account.CalculateAccountBalanceUseCase
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.exchange.ExchangeData
import java.math.BigDecimal
import javax.inject.Inject

class CalculateWalletBalanceUseCase @Inject internal constructor(
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val calculateAccountBalanceUseCase: CalculateAccountBalanceUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
) {
    suspend operator fun invoke(
        baseCurrency: String,
        balanceCurrency: String = baseCurrency,
        range: ClosedTimeRange? = null,
        withExcluded: Boolean = false
    ): BigDecimal {
        return getLegacyAccountsUseCase()
            .filter { withExcluded || it.includeInBalance }
            .fold(BigDecimal.ZERO) { sum, account ->
                val domainAccount = Account(
                    id = AccountId(account.id),
                    name = NotBlankTrimmedString.from(account.name).getOrNull()
                        ?: error("account name cannot be blank"),
                    asset = AssetCode.from(account.currency ?: baseCurrency).getOrNull()
                        ?: error("account currency cannot be blank"),
                    color = ColorInt(account.color),
                    icon = account.icon?.let { IconAsset.from(it).getOrNull() },
                    includeInBalance = account.includeInBalance,
                    orderNum = account.orderNum,
                )

                val accountBalance = calculateAccountBalanceUseCase(
                    account = domainAccount,
                    range = range
                )
                val exchanged = exchangeAmountUseCase(
                    data = ExchangeData(
                        baseCurrency = baseCurrency,
                        fromCurrency = domainAccount.asset.code.toOption(),
                        toCurrency = balanceCurrency
                    ),
                    amount = accountBalance
                )

                sum + (exchanged.getOrNull() ?: BigDecimal.ZERO)
            }
    }
}

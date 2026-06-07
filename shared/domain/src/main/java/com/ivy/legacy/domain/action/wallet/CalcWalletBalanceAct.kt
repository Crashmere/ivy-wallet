package com.ivy.legacy.domain.action.wallet

import arrow.core.toOption
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.legacy.frp.action.FPAction
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.domain.usecase.account.CalculateAccountBalanceUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.legacy.domain.pure.exchange.ExchangeData
import java.math.BigDecimal
import javax.inject.Inject

class CalcWalletBalanceAct @Inject constructor(
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val calculateAccountBalanceUseCase: CalculateAccountBalanceUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
) : FPAction<CalcWalletBalanceAct.Input, BigDecimal>() {

    override suspend fun Input.compose(): suspend () -> BigDecimal = suspend {
        getLegacyAccountsUseCase()
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

    @Suppress("DataClassDefaultValues")
    data class Input(
        val baseCurrency: String,
        val balanceCurrency: String = baseCurrency,
        val range: ClosedTimeRange? = null,
        val withExcluded: Boolean = false
    )
}

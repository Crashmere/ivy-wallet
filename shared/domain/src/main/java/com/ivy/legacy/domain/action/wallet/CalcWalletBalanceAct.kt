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
import com.ivy.legacy.domain.action.account.CalcAccBalanceAct
import com.ivy.legacy.domain.action.exchange.ExchangeAct
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.legacy.domain.pure.exchange.ExchangeData
import java.math.BigDecimal
import javax.inject.Inject

class CalcWalletBalanceAct @Inject constructor(
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val calcAccBalanceAct: CalcAccBalanceAct,
    private val exchangeAct: ExchangeAct,
) : FPAction<CalcWalletBalanceAct.Input, BigDecimal>() {

    override suspend fun Input.compose(): suspend () -> BigDecimal = suspend {
        getLegacyAccountsUseCase()
            .filter { withExcluded || it.includeInBalance }
            .fold(BigDecimal.ZERO) { sum, account ->
                val accountBalance = calcAccBalanceAct(
                    CalcAccBalanceAct.Input(
                        account = Account(
                            id = AccountId(account.id),
                            name = NotBlankTrimmedString.from(account.name).getOrNull()
                                ?: error("account name cannot be blank"),
                            asset = AssetCode.from(account.currency ?: baseCurrency).getOrNull()
                                ?: error("account currency cannot be blank"),
                            color = ColorInt(account.color),
                            icon = account.icon?.let { IconAsset.from(it).getOrNull() },
                            includeInBalance = account.includeInBalance,
                            orderNum = account.orderNum,
                        ),
                        range = range
                    )
                )

                val exchanged = exchangeAct(
                    ExchangeAct.Input(
                        data = ExchangeData(
                            baseCurrency = baseCurrency,
                            fromCurrency = accountBalance.account.asset.code.toOption(),
                            toCurrency = balanceCurrency
                        ),
                        amount = accountBalance.balance
                    )
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

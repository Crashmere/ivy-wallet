package com.ivy.domain.usecase.wallet

import arrow.core.toOption
import com.ivy.data.model.ClosedTimeRange
import com.ivy.domain.usecase.account.CalculateAccountBalanceUseCase
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.exchange.ExchangeData
import java.math.BigDecimal
import javax.inject.Inject

class CalculateWalletBalanceUseCase @Inject internal constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val calculateAccountBalanceUseCase: CalculateAccountBalanceUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
) {
    suspend operator fun invoke(
        baseCurrency: String,
        balanceCurrency: String = baseCurrency,
        range: ClosedTimeRange? = null,
        withExcluded: Boolean = false
    ): BigDecimal {
        return getAccountsUseCase()
            .filter { withExcluded || it.includeInBalance }
            .fold(BigDecimal.ZERO) { sum, account ->
                val accountBalance = calculateAccountBalanceUseCase(
                    account = account,
                    range = range
                )
                val exchanged = exchangeAmountUseCase(
                    data = ExchangeData(
                        baseCurrency = baseCurrency,
                        fromCurrency = account.asset.code.toOption(),
                        toCurrency = balanceCurrency
                    ),
                    amount = accountBalance
                )

                sum + (exchanged.getOrNull() ?: BigDecimal.ZERO)
            }
    }
}

package com.ivy.domain.usecase.planned

import com.ivy.data.api.AccountStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Account
import com.ivy.data.model.TransactionType
import com.ivy.data.model.FromToTimeRange
import com.ivy.data.model.Transaction
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getFromValue
import com.ivy.data.model.getTransactionType
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CalculatePlannedPaymentsAmountForRangeUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val accountStore: AccountStore,
) {
    suspend operator fun invoke(range: FromToTimeRange): Double {
        return withContext(Dispatchers.IO) {
            val baseCurrency = getBaseCurrencyCode()
            val accounts = accountStore.findAll()

            transactionStore.findAllDueToBetween(
                startDate = range.from(),
                endDate = range.to()
            ).sumOf { transaction ->
                val amount = transaction.amountBaseCurrency(
                    baseCurrency = baseCurrency,
                    accounts = accounts
                )

                when (transaction.getTransactionType()) {
                    TransactionType.INCOME -> amount
                    TransactionType.EXPENSE -> -amount
                    TransactionType.TRANSFER -> 0.0
                }
            }
        }
    }

    private suspend fun Transaction.amountBaseCurrency(
        baseCurrency: String,
        accounts: List<Account>,
    ): Double {
        val amount = getFromValue().amount.value
        val amountCurrency = accounts.find { it.id == getFromAccount() }?.asset?.code
            ?: return amount
        return exchangeAmountUseCase(
            amount = amount.toBigDecimal(),
            baseCurrency = baseCurrency,
            fromCurrency = amountCurrency,
        ).getOrNull()?.toDouble() ?: amount
    }
}

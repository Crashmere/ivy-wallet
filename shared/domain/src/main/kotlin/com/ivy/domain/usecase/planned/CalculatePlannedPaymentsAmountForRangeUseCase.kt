package com.ivy.domain.usecase.planned

import com.ivy.data.api.AccountStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.TransactionType
import com.ivy.data.model.FromToTimeRange
import com.ivy.domain.mapper.legacy.toLegacyAccount
import com.ivy.domain.mapper.legacy.toLegacy
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CalculatePlannedPaymentsAmountForRangeUseCase @Inject constructor(
    private val transactionStore: TransactionStore,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeRatesUseCase: LegacyExchangeRatesUseCase,
    private val accountStore: AccountStore,
) {
    suspend operator fun invoke(range: FromToTimeRange): Double {
        return withContext(Dispatchers.IO) {
            val baseCurrency = getBaseCurrencyCode()
            val accounts = accountStore.findAll().map { it.toLegacyAccount() }

            transactionStore.findAllDueToBetween(
                startDate = range.from(),
                endDate = range.to()
            ).sumOf { transaction ->
                val legacyTransaction = transaction.toLegacy()
                val amount = exchangeRatesUseCase.amountBaseCurrency(
                    transaction = legacyTransaction,
                    baseCurrency = baseCurrency,
                    accounts = accounts
                )

                when (legacyTransaction.type) {
                    TransactionType.INCOME -> amount
                    TransactionType.EXPENSE -> -amount
                    TransactionType.TRANSFER -> 0.0
                }
            }
        }
    }
}

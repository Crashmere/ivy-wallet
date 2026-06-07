package com.ivy.domain.usecase.planned

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.AccountStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.legacy.FromToTimeRange
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.domain.mapper.legacy.toLegacy
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import com.ivy.domain.mapper.legacy.toLegacyDomain
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CalculatePlannedPaymentsAmountForRangeUseCase @Inject constructor(
    private val transactionStore: TransactionStore,
    private val transactionMapper: TransactionMapper,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeRatesLogic: LegacyExchangeRatesUseCase,
    private val accountStore: AccountStore,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(range: FromToTimeRange): Double {
        return withContext(dispatchers.io) {
            val baseCurrency = getBaseCurrencyCode()
            val accounts = accountStore.findAll().map { it.toLegacyDomain() }

            transactionStore.findAllDueToBetween(
                startDate = range.from(),
                endDate = range.to()
            ).sumOf { transaction ->
                val legacyTransaction = transaction.toLegacy(transactionMapper)
                val amount = exchangeRatesLogic.amountBaseCurrency(
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

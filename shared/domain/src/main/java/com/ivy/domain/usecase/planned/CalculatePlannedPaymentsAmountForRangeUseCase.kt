package com.ivy.domain.usecase.planned

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.db.dao.read.TransactionDao
import com.ivy.data.model.legacy.FromToTimeRange
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import com.ivy.domain.mapper.legacy.toLegacyDomain
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CalculatePlannedPaymentsAmountForRangeUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val exchangeRatesLogic: LegacyExchangeRatesUseCase,
    private val accountDao: AccountDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(range: FromToTimeRange): Double {
        return withContext(dispatchers.io) {
            val baseCurrency = getBaseCurrencyCode()
            val accounts = accountDao.findAll().map { it.toLegacyDomain() }

            transactionDao.findAllDueToBetween(
                startDate = range.from(),
                endDate = range.to()
            ).sumOf { transaction ->
                val amount = exchangeRatesLogic.amountBaseCurrency(
                    transaction = transaction.toLegacyDomain(),
                    baseCurrency = baseCurrency,
                    accounts = accounts
                )

                when (transaction.type) {
                    TransactionType.INCOME -> amount
                    TransactionType.EXPENSE -> -amount
                    TransactionType.TRANSFER -> 0.0
                }
            }
        }
    }
}

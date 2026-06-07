package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.Transaction
import com.ivy.base.model.legacy.TransactionHistoryItem
import com.ivy.base.time.TimeConverter
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.mapper.legacy.toLegacyDomain
import com.ivy.domain.transaction.legacy.LegacyTrnDateDividers
import javax.inject.Inject

class BuildLegacyTransactionHistoryItemsUseCase @Inject constructor(
    private val accountDao: AccountDao,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val timeConverter: TimeConverter,
) {
    suspend operator fun invoke(
        baseCurrency: String,
        transactions: List<Transaction>
    ): List<TransactionHistoryItem> {
        return LegacyTrnDateDividers.transactionsWithDateDividers(
            transactions = transactions,
            baseCurrencyCode = baseCurrency,
            getAccount = { accountId -> accountDao.findById(accountId)?.toLegacyDomain() },
            exchange = exchangeAmountUseCase::invoke,
            timeConverter = timeConverter,
        )
    }
}

package com.ivy.domain.usecase.transaction

import com.ivy.data.model.legacy.Transaction
import com.ivy.data.model.legacy.TransactionHistoryItem
import com.ivy.base.time.TimeConverter
import com.ivy.data.api.AccountStore
import com.ivy.data.model.AccountId
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.mapper.legacy.toLegacyDomain
import com.ivy.domain.transaction.legacy.LegacyTrnDateDividers
import javax.inject.Inject

class BuildLegacyTransactionHistoryItemsUseCase @Inject constructor(
    private val accountStore: AccountStore,
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
            getAccount = { accountId -> accountStore.findById(AccountId(accountId))?.toLegacyDomain() },
            exchange = exchangeAmountUseCase::invoke,
            timeConverter = timeConverter,
        )
    }
}

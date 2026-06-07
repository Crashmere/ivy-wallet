package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.TransactionHistoryItem
import com.ivy.data.api.AccountStore
import com.ivy.data.api.TagStore
import com.ivy.data.model.AccountId
import com.ivy.data.model.Transaction
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.mapper.legacy.toLegacyDomain
import com.ivy.domain.transaction.legacy.transactionsWithDateDividers
import javax.inject.Inject

class BuildTransactionHistoryItemsUseCase @Inject constructor(
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val tagStore: TagStore,
    private val accountStore: AccountStore,
) {
    suspend operator fun invoke(
        baseCurrency: String,
        transactions: List<Transaction>
    ): List<TransactionHistoryItem> {
        return transactionsWithDateDividers(
            transactions = transactions,
            baseCurrencyCode = baseCurrency,
            getTags = { tagIds -> tagStore.findByIds(tagIds) },
            getAccount = { accountId -> accountStore.findById(AccountId(accountId))?.toLegacyDomain() },
            accountStore = accountStore,
            exchange = exchangeAmountUseCase::invoke
        )
    }
}

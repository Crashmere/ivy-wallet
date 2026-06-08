package com.ivy.domain.usecase.transaction

import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.api.AccountStore
import com.ivy.data.model.AccountId
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.mapper.legacy.toLegacyAccount
import com.ivy.domain.transaction.legacy.LegacyTransactionDateDividers
import javax.inject.Inject

class BuildLegacyTransactionHistoryItemsUseCase @Inject constructor(
    private val accountStore: AccountStore,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
) {
    suspend operator fun invoke(
        baseCurrency: String,
        transactions: List<LegacyTransaction>
    ): List<TransactionHistoryItem> {
        return LegacyTransactionDateDividers.transactionsWithDateDividers(
            transactions = transactions,
            baseCurrencyCode = baseCurrency,
            getAccount = { accountId -> accountStore.findById(AccountId(accountId))?.toLegacyAccount() },
            exchange = exchangeAmountUseCase::invoke,
        )
    }
}

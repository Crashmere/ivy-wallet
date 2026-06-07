package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.TransactionHistoryItem
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.model.Transaction
import com.ivy.data.repository.AccountRepository
import com.ivy.data.repository.TagRepository
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.domain.mapper.legacy.toLegacyDomain
import com.ivy.domain.transaction.legacy.transactionsWithDateDividers
import javax.inject.Inject

class BuildTransactionHistoryItemsUseCase @Inject constructor(
    private val accountDao: AccountDao,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val tagRepository: TagRepository,
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(
        baseCurrency: String,
        transactions: List<Transaction>
    ): List<TransactionHistoryItem> {
        return transactionsWithDateDividers(
            transactions = transactions,
            baseCurrencyCode = baseCurrency,
            getTags = { tagIds -> tagRepository.findByIds(tagIds) },
            getAccount = { accountId -> accountDao.findById(accountId)?.toLegacyDomain() },
            accountRepository = accountRepository,
            exchange = exchangeAmountUseCase::invoke
        )
    }
}

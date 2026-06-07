package com.ivy.legacy.domain.action.transaction

import com.ivy.base.model.legacy.Transaction
import com.ivy.base.model.legacy.TransactionHistoryItem
import com.ivy.base.time.TimeConverter
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.repository.AccountRepository
import com.ivy.data.repository.TagRepository
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.frp.then
import com.ivy.legacy.domain.mapper.toLegacyDomain
import com.ivy.legacy.domain.pure.transaction.LegacyTrnDateDividers
import com.ivy.legacy.domain.pure.transaction.transactionsWithDateDividers
import javax.inject.Inject

class TrnsWithDateDivsAct @Inject constructor(
    private val accountDao: AccountDao,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val tagRepository: TagRepository,
    private val accountRepository: AccountRepository,
) : FPAction<TrnsWithDateDivsAct.Input, List<TransactionHistoryItem>>() {

    override suspend fun Input.compose(): suspend () -> List<TransactionHistoryItem> = suspend {
        transactionsWithDateDividers(
            transactions = transactions,
            baseCurrencyCode = baseCurrency,
            getTags = { tagIds -> tagRepository.findByIds(tagIds) },
            getAccount = accountDao::findById then { it?.toLegacyDomain() },
            accountRepository = accountRepository,
            exchange = exchangeAmountUseCase::invoke
        )
    }

    data class Input(
        val baseCurrency: String,
        val transactions: List<com.ivy.data.model.Transaction>
    )
}

class LegacyTrnsWithDateDivsAct @Inject constructor(
    private val accountDao: AccountDao,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val timeConverter: TimeConverter,
) : FPAction<LegacyTrnsWithDateDivsAct.Input, List<TransactionHistoryItem>>() {

    override suspend fun Input.compose(): suspend () -> List<TransactionHistoryItem> = suspend {
        LegacyTrnDateDividers.transactionsWithDateDividers(
            transactions = transactions,
            baseCurrencyCode = baseCurrency,

            getAccount = accountDao::findById then { it?.toLegacyDomain() },
            exchange = exchangeAmountUseCase::invoke,
            timeConverter = timeConverter,
        )
    }

    data class Input(
        val baseCurrency: String,
        val transactions: List<Transaction>
    )
}

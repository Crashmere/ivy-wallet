package com.ivy.domain.transaction.legacy

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.model.TransactionType
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.domain.account.legacy.legacyAccountCurrency

object LegacyTransactionFunctions {
    fun expenses(transactions: List<com.ivy.data.model.legacy.LegacyTransaction>): List<com.ivy.data.model.legacy.LegacyTransaction> {
        return transactions.filter { it.type == TransactionType.EXPENSE }
    }
    fun incomes(transactions: List<com.ivy.data.model.legacy.LegacyTransaction>): List<com.ivy.data.model.legacy.LegacyTransaction> {
        return transactions.filter { it.type == TransactionType.INCOME }
    }
    fun transactionCurrency(
        transaction: com.ivy.data.model.legacy.LegacyTransaction,
        accounts: List<LegacyAccount>,
        baseCurrency: String
    ): Option<String> {
        val account = accounts.find { it.id == transaction.accountId }
            ?: return baseCurrency.toOption()
        return legacyAccountCurrency(account, baseCurrency).toOption()
    }
}

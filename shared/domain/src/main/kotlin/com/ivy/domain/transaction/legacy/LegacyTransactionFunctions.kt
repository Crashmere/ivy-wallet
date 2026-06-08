package com.ivy.domain.transaction.legacy
import com.ivy.data.model.legacy.LegacyTransaction

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.model.TransactionType
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.legacy.legacyAccountCurrency

internal object LegacyTransactionFunctions {
    fun expenses(transactions: List<LegacyTransaction>): List<LegacyTransaction> {
        return transactions.filter { it.type == TransactionType.EXPENSE }
    }
    fun incomes(transactions: List<LegacyTransaction>): List<LegacyTransaction> {
        return transactions.filter { it.type == TransactionType.INCOME }
    }
    fun transactionCurrency(
        transaction: LegacyTransaction,
        accounts: List<LegacyAccount>,
        baseCurrency: String
    ): Option<String> {
        val account = accounts.find { it.id == transaction.accountId }
            ?: return baseCurrency.toOption()
        return legacyAccountCurrency(account, baseCurrency).toOption()
    }
}

package com.ivy.domain.transaction.legacy

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Transaction
import com.ivy.data.model.legacy.Account
import com.ivy.domain.account.accountCurrency
import com.ivy.domain.transaction.getAccountId

fun trnCurrency(
    transaction: Transaction,
    accounts: List<Account>,
    baseCurrency: String
): Option<String> {
    val account = accounts.find {
        it.id == transaction.getAccountId()
    }
        ?: return baseCurrency.toOption()
    return accountCurrency(account, baseCurrency).toOption()
}

object LegacyTrnFunctions {
    fun expenses(transactions: List<com.ivy.data.model.legacy.Transaction>): List<com.ivy.data.model.legacy.Transaction> {
        return transactions.filter { it.type == TransactionType.EXPENSE }
    }
    fun incomes(transactions: List<com.ivy.data.model.legacy.Transaction>): List<com.ivy.data.model.legacy.Transaction> {
        return transactions.filter { it.type == TransactionType.INCOME }
    }
    fun trnCurrency(
        transaction: com.ivy.data.model.legacy.Transaction,
        accounts: List<Account>,
        baseCurrency: String
    ): Option<String> {
        val account = accounts.find { it.id == transaction.accountId }
            ?: return baseCurrency.toOption()
        return accountCurrency(account, baseCurrency).toOption()
    }
}
